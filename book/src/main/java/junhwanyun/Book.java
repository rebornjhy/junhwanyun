package junhwanyun;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Book_table")
public class Book {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Integer stock;
    private String name;
    private Double version;

    @PostPersist
    public void onPostPersist() throws InterruptedException {
        Thread.sleep((long) (444 + Math.random() * 444)); // 444ms +-444ms delay
        
    }

    @PostUpdate
    public void onPostUpdate() {
        Updated updated = new Updated();
        BeanUtils.copyProperties(this, updated);
        updated.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }




}
