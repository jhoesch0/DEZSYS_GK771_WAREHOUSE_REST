package rest.warehouse;

import org.springframework.stereotype.Service;
import rest.model.WarehouseData;

@Service
public class WarehouseService {
	
	public String getGreetings( String inModule ) {
        return "Greetings from " + inModule;
    }

    public WarehouseData getWarehouseData( String inID,String name ) {
    	
    	WarehouseSimulation simulation = new WarehouseSimulation();
        return simulation.getData( inID,name );
        
    }
    
}