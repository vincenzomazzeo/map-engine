package it.alidays.mapengine.test;

import it.alidays.mapengine.core.map.Aggregator;
import it.alidays.mapengine.core.map.AggregatorException;
import it.alidays.mapengine.test.bm.Catalog;
import it.alidays.mapengine.test.bm.generated.CatalogMap;
import it.alidays.mapengine.test.bm.generated.MapEngineRetrieveId;

import java.util.ArrayList;
import java.util.List;

public class CatalogAggregator implements Aggregator {

	private final List<Catalog> result;
	
	public CatalogAggregator() {
		this.result = new ArrayList<>();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void notifyRetrieveResult(String id, List<Object> data) throws AggregatorException {
		switch (MapEngineRetrieveId.valueOf(id)) {
		case Catalog:
			manageCatalog((List<CatalogMap>)(List<?>)data);
			break;
		}
	}

	@Override
	public Object getMapResult() throws AggregatorException {
		return this.result;
	}
	
	private void manageCatalog(List<CatalogMap> items) {
		for (CatalogMap item : items) {
			Catalog catalog = new Catalog();
			this.result.add(catalog);
			catalog.setDescription(item.getDescription());
			catalog.setImage(item.getProductImage());
			catalog.setGender(item.getGender());
			catalog.setItemNumber(item.getItemNumber());
			catalog.setPrice(item.getPrice());
			catalog.setItemSizeDescription(item.getItemSizeDescription());
			catalog.setItemSizeColorImage(item.getImage());
			catalog.setItemSizeColorColor(item.getColor());
		}
	}

}
