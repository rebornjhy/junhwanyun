package junhwanyun;

import junhwanyun.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired
    BookRepository bookRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRenewed_(@Payload Renewed renewed){

        if(renewed.isMe()){
            System.out.println("##### listener  : " + renewed.toJson());
            //
            Book book = bookRepository.findById(renewed.getBookId()).get();

            book.setId(renewed.getBookId());
            book.setVersion(renewed.getVersion());
            book.setStock(100); // 100권 입고

            bookRepository.save(book);
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverWarehoused_(@Payload Warehoused warehoused) {
        if(warehoused.isMe()){
            System.out.println("##### listener  : " + warehoused.toJson());
            //
            Book book = bookRepository.findById(warehoused.getBookId()).get();

            book.setId(warehoused.getBookId());
            book.setStock(warehoused.getQty());
            book.setVersion(warehoused.getVersion());

            bookRepository.save(book);
        }
    }
}