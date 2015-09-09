/*
 * Copyright 2015 Alidays S.p.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.alidays.mapengine.codegenerator;

import it.alidays.mapengine.configuration.Configuration;
import it.alidays.mapengine.core.database.DatabaseManager;
import it.alidays.mapengine.core.map.AbstractRetrieve;
import it.alidays.mapengine.core.map.RetrieveHandler;
import it.alidays.mapengine.core.schema.SchemaHandler;
import it.alidays.mapengine.core.schema.SchemaHandlerException;
import it.alidays.mapengine.core.schema.converter.TypeConverterFactory;
import it.alidays.mapengine.core.schema.converter.TypeConverterFactoryException;
import it.alidays.mapengine.enginedirectives.EngineDirectives;
import it.alidays.mapengine.enginedirectives.map.Retrieve;
import it.alidays.mapengine.util.ResourceRetriever;
import it.alidays.mapengine.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

public class MapperEngineCodeGenerator {

	private static final Logger logger = LoggerFactory.getLogger(MapperEngineCodeGenerator.class);

	public static void main(String[] args) throws MapperEngineCodeGeneratorException {
		if (args.length != 2) {
			printUsage();
		}
		else {
			String destinationDir = null;
			String[] engineDirectivesSources = null;

			for (String arg : args) {
				if (arg.startsWith("-d")) {
					destinationDir = arg.substring(2);
				}
				else if (arg.startsWith("-s")) {
					engineDirectivesSources = arg.substring(2).split("\\|");
				}
			}

			if (destinationDir == null || destinationDir.isEmpty() || engineDirectivesSources == null || engineDirectivesSources.length == 0) {
				printUsage();
			}
			else {
				for (String engineDirectivesSource : engineDirectivesSources) {
					run(new File(destinationDir), MapperEngineCodeGenerator.class.getClassLoader().getResourceAsStream(engineDirectivesSource));
				}
			}
		}
	}

	private static void run(File destinationDir, InputStream engineDirectivesSource) throws MapperEngineCodeGeneratorException {
		logger.info("Code generation started...");

		/*****************
		 * CONFIGURATION *
		 *****************/
		Configuration configuration;
		try {
			configuration = ResourceRetriever.loadConfiguration();
		}
		catch (JAXBException jaxbe) {
			throw new MapperEngineCodeGeneratorException("Failed to load configuration", jaxbe);
		}
		/**************************
		 * TYPE CONVERTER FACTORY *
		 **************************/
		try {
			TypeConverterFactory.initialize(configuration);
		}
		catch (TypeConverterFactoryException tcfe) {
			throw new MapperEngineCodeGeneratorException("Failed initialize TypeConverterFactory", tcfe);
		}
		/*********************
		 * ENGINE DIRECTIVES *
		 *********************/
		EngineDirectives engineDirectives;
		try {
			engineDirectives = ResourceRetriever.loadEngineDirectives(engineDirectivesSource);
		}
		catch (JAXBException jaxbe) {
			throw new MapperEngineCodeGeneratorException("Failed to load engine directives", jaxbe);
		}

		DatabaseManager databaseManager = null;
		try {
			databaseManager = new DatabaseManager("jdbc:h2:mem:mapgenerator;DB_CLOSE_DELAY=-1", "mapgenerator", "mapgenerator");
		}
		catch (SQLException sqle) {
			throw new MapperEngineCodeGeneratorException("Failed to initialize database manager", sqle);
		}

		SchemaHandler schemaHandler = null;
		try {
			schemaHandler = new SchemaHandler(engineDirectives.getFetch().getEntities(), databaseManager);
			schemaHandler.create();
		}
		catch (SchemaHandlerException she) {
			throw new MapperEngineCodeGeneratorException(she);
		}

		logger.info("--------------------------------------");
		logger.info("*** GENERATING MAP CLASSES ***");

		String packageName = engineDirectives.getMap().getMapPackage();

		File realDestinationDir = new File(destinationDir, packageName.replaceAll("\\.", "/"));
		if (!realDestinationDir.exists()) {
			realDestinationDir.mkdirs();
		}
		File[] files = realDestinationDir.listFiles();
		for (File file : files) {
			file.delete();
		}

		if (engineDirectives.getMap().getRetrieves() != null) {
			try (Connection connection = databaseManager.getConnection()) {
				for (Retrieve retrieve : engineDirectives.getMap().getRetrieves()) {
					manageRetrieve(retrieve, connection, packageName, destinationDir);
				}
			}
			catch (Exception e) {
				throw new MapperEngineCodeGeneratorException(e);
			}
		}

		logger.info("--------------------------------------");
		logger.info("*** GENERATING RETRIEVE ID ENUM ***");

		if (engineDirectives.getMap().getRetrieves() != null) {
			try {
				createRetrieveIdEnum(engineDirectives.getMap().getRetrieves(), packageName, destinationDir);
			}
			catch (Exception e) {
				throw new MapperEngineCodeGeneratorException(e);
			}
		}

		logger.info("Code generation completed");
	}

	private static void manageRetrieve(Retrieve retrieve, Connection connection, String packageName, File destinationDir) throws SQLException,
																														 JClassAlreadyExistsException,
																														 IOException,
																														 MapperEngineCodeGeneratorException {
		logger.info("Generating map for {}", retrieve.getId());

		int vuidCount = RetrieveHandler.getVuidCount(retrieve.getContent());
		String content = retrieve.getContent().replaceAll(RetrieveHandler.VUID_KEY, "?");

		Map<String, Integer> columns = new LinkedHashMap<>();

		logger.info("\tRetrieving columns' name");
		try (PreparedStatement preparedStatement = connection.prepareStatement(content)) {
			for (int index = 1; index <= vuidCount; index++) {
				preparedStatement.setObject(index, "_");
			}

			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			for (int i = 1, n = resultSetMetaData.getColumnCount(); i <= n; i++) {
				String columnName = Utils.arrangeColumnName(resultSetMetaData.getColumnLabel(i));
				Integer columnType = resultSetMetaData.getColumnType(i);
				columns.put(columnName, columnType);
			}
		}
		logger.info("\tRetrieved {} columns' name", columns.size());

		createMapClass(retrieve, columns, packageName, destinationDir);
		createRetrieveClass(retrieve, packageName, destinationDir);

		logger.info("Map successfully generated for {}", retrieve.getId());
	}

	private static void createMapClass(Retrieve retrieve, Map<String, Integer> columns, String packageName, File destinationDir) throws JClassAlreadyExistsException,
																																MapperEngineCodeGeneratorException,
																																IOException {
		JCodeModel codeModel = new JCodeModel();
		JDefinedClass mapClass = codeModel._class(String.format("%s.%sMap", packageName, retrieve.getId()));
		mapClass.javadoc().append("Auto generated class. Do not modify!");

		// Constructor
		JMethod constructor = mapClass.constructor(JMod.PROTECTED);
		constructor.param(codeModel.ref(Map.class).narrow(String.class, Object.class), "data");

		for (String column : columns.keySet()) {
			String varName = WordUtils.uncapitalize(column);
			Class<?> type = null;
			switch (columns.get(column)) {
			case Types.INTEGER:
				type = Integer.class;
				break;
			case Types.VARCHAR:
				type = String.class;
				break;
			case Types.DECIMAL:
				type = BigDecimal.class;
				break;
			default:
				throw new MapperEngineCodeGeneratorException(String.format("Missing Type map for column %s: %d", column, columns.get(column)));
			}

			JFieldVar field = mapClass.field(JMod.PRIVATE | JMod.FINAL, type, varName);

			// Getter
			JMethod getter = mapClass.method(JMod.PUBLIC, type, String.format("get%s", column));
			getter.body()._return(JExpr._this().ref(field));

			constructor.body().assign(JExpr._this().ref(varName), JExpr.cast(codeModel.ref(type), JExpr.ref("data").invoke("get").arg(column)));
		}
		codeModel.build(destinationDir);
	}

	private static void createRetrieveClass(Retrieve retrieve, String packageName, File destinationDir) throws JClassAlreadyExistsException, IOException {
		JCodeModel codeModel = new JCodeModel();

		JClass mapClass = codeModel.ref(String.format("%s.%sMap", packageName, retrieve.getId()));

		JDefinedClass retrieveClass = codeModel._class(String.format("%s.%sRetrieve", packageName, retrieve.getId()));
		retrieveClass.javadoc().append("Auto generated class. Do not modify!");
		retrieveClass._extends(codeModel.ref(AbstractRetrieve.class).narrow(mapClass));

		// Constructor
		JMethod constructor = retrieveClass.constructor(JMod.PUBLIC);
		constructor.param(String.class, "id");
		constructor.body().invoke("super").arg(JExpr.ref("id"));

		// Implemented method
		JMethod getMapMethod = retrieveClass.method(JMod.PUBLIC, mapClass, "getMap");
		getMapMethod.annotate(Override.class);
		getMapMethod.param(codeModel.ref(Map.class).narrow(String.class, Object.class), "data");
		getMapMethod.body()._return(JExpr._new(mapClass).arg(JExpr.ref("data")));

		codeModel.build(destinationDir);
	}

	private static void createRetrieveIdEnum(List<Retrieve> retrieves, String packageName, File destinationDir) throws JClassAlreadyExistsException, IOException {
		JCodeModel codeModel = new JCodeModel();
		JDefinedClass enumClass = codeModel._class(JMod.PUBLIC, String.format("%s.MapEngineRetrieveId", packageName), ClassType.ENUM);

		for (Retrieve retrieve : retrieves) {
			enumClass.enumConstant(retrieve.getId());
		}

		codeModel.build(destinationDir);
	}

	private static void printUsage() {
		System.out.println("MapperEngineCodeGenerator -d[destination-dir] -s[engine-directives-sources]");
	}

	private MapperEngineCodeGenerator() {
	}

}
