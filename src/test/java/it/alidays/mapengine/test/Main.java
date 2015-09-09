package it.alidays.mapengine.test;

import it.alidays.mapengine.core.Engine;
import it.alidays.mapengine.test.bm.Catalog;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.List;

public class Main {

	public static void main(String[] args) throws Exception {
		Engine engine = new Engine(Engine.class.getClassLoader().getResourceAsStream("create_catalog_directives.xml"));

		StringBuilder xml = new StringBuilder();
		byte[] buffer = new byte[4096];
		int readed = 0;
		try (BufferedInputStream bis = new BufferedInputStream(Main.class.getClassLoader().getResourceAsStream("in.xml"))) {
			while ((readed = bis.read(buffer)) != -1) {
				xml.append(new String(buffer, 0, readed));
			}
		}
		
		@SuppressWarnings("unchecked")
		List<Catalog> result = (List<Catalog>)engine.run(new ByteArrayInputStream(xml.toString().getBytes()));
		
		for (Catalog catalog : result) {
			System.out.println(catalog);
		}
			
		engine.shutdown();
	}

}
