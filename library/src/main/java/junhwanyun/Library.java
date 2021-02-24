package junhwanyun;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

import junhwanyun.external.Book;
import junhwanyun.external.BookService;

@Entity
@Table(name="Library_table")
public class Library {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long bookId;
    private Integer qty;
    private Double version;

    @PrePersist
    public void onPrePersist(){
        Warehoused warehoused = new Warehoused();
        BeanUtils.copyProperties(this, warehoused);
        warehoused.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        Book book = new Book();
        
        // mappings goes here
        LibraryApplication.applicationContext.getBean(BookService.class)
            .receive(book);


    }

    @PostUpdate
    public void onPostUpdate(){
        Renewed renewed = new Renewed();
        BeanUtils.copyProperties(this, renewed);
        renewed.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }




}
