package kr.ac.dankook.StockSimulationGame2026.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Getter @Setter
@NoArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 하이버네이트가 자동으로 stock_code로 매핑합니다.
    private String stockCode;
    private String stockName;

    // 자동으로 st_open, st_high 등으로 매핑됩니다.
    private Double stOpen;
    private Double stHigh;
    private Double stLow;
    private Double stClose;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stDate;
}