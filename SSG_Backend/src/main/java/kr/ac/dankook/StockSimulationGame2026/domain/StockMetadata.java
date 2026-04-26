package kr.ac.dankook.StockSimulationGame2026.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_metadata")
@Getter @Setter
@NoArgsConstructor
public class StockMetadata {
    @Id
    private String stockCode;
    private String stockName;
    private Double baseVolatility;
}
