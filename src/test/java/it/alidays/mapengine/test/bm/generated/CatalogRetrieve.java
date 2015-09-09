
package it.alidays.mapengine.test.bm.generated;

import java.util.Map;
import it.alidays.mapengine.core.map.AbstractRetrieve;


/**
 * Auto generated class. Do not modify!
 * 
 */
public class CatalogRetrieve
    extends AbstractRetrieve<CatalogMap>
{


    public CatalogRetrieve(String id) {
        super(id);
    }

    @Override
    public CatalogMap getMap(Map<String, Object> data) {
        return new CatalogMap(data);
    }

}
