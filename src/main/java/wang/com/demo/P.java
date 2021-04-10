package wang.com.demo;

import wang.com.type.AutoLog;
import wang.com.type.Autowired;
import wang.com.type.Component;

@Component
public class P {
	@Autowired
	private S s;

    @AutoLog
    public String getSName(){
        return s.name;
    }
}