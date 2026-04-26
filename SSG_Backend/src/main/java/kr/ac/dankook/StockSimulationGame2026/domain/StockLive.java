package kr.ac.dankook.StockSimulationGame2026.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_live")
@Getter @Setter
@NoArgsConstructor
public class StockLive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockCode;
    private Double stClose;
    private LocalDateTime stDate;
}
