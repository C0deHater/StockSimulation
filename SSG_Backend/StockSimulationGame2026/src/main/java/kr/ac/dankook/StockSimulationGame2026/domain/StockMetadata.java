package kr.ac.dankook.StockSimulationGame2026.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_metadata") // DB 테이블 이름과 매칭
@Getter @Setter
@NoArgsConstructor
public class StockMetadata {
    @Id
    private String stockCode;    // 종목 코드 (PK)
    private String stockName;    // 종목명
    private Double baseVolatility; // 아까 SQL로 구한 변동성 기초값
}