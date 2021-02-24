
package junhwanyun.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="book", url="http://localhost:8083") //url="http://book:8080")
public interface BookService {

    @RequestMapping(method= RequestMethod.POST, path="/books")
    public void receive(@RequestBody Book book);

}