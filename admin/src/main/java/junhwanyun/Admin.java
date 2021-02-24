package junhwanyun;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="Admin_table")
public class Admin {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long bookId;
        private Double bookVersion;
        private Integer bookStock;


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
        public Double getBookVersion() {
            return bookVersion;
        }

        public void setBookVersion(Double bookVersion) {
            this.bookVersion = bookVersion;
        }
        public Integer getBookStock() {
            return bookStock;
        }

        public void setBookStock(Integer bookStock) {
            this.bookStock = bookStock;
        }

}
