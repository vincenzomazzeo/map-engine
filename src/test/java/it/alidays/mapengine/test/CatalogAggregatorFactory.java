package it.alidays.mapengine.test;

import it.alidays.mapengine.core.map.Aggregator;
import it.alidays.mapengine.core.map.AggregatorFactory;

public class CatalogAggregatorFactory implements AggregatorFactory {

	@Override
	public Aggregator make() {
		return new CatalogAggregator();
	}

}
