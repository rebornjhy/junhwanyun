package junhwanyun;

import junhwanyun.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AdminViewHandler {


    @Autowired
    private AdminRepository adminRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenWarehoused_then_CREATE_1 (@Payload Warehoused warehoused) {
        try {
            if (warehoused.isMe()) {
                // view 객체 생성
                Admin admin = new Admin();
                // view 객체에 이벤트의 Value 를 set 함
                admin.setBookId(warehoused.getBookId());
                admin.setBookVersion(warehoused.getVersion());
                admin.setBookStock(warehoused.getQty());
                // view 레파지 토리에 save
                adminRepository.save(admin);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenUpdated_then_UPDATE_1(@Payload Updated updated) {
        try {
            if (updated.isMe()) {
                // view 객체 조회
                Admin admin = adminRepository.findById(updated.getId()).get();

                admin.setBookStock(updated.getStock());
                admin.setBookVersion(updated.getVersion());
                
                adminRepository.save(admin);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}