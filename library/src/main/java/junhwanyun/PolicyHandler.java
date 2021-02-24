package junhwanyun;

import junhwanyun.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler {
    @Autowired
    LibraryRepository libraryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverWarehoused_(@Payload Warehoused warehoused) {
        if(warehoused.isMe()){
            System.out.println("##### listener  : " + warehoused.toJson());
            //
            Library library = libraryRepository.findById(warehoused.getBookId()).get();

            library.setQty(warehoused.getQty());
            library.setVersion(warehoused.getVersion());

            libraryRepository.save(library);
        }
    }
}
