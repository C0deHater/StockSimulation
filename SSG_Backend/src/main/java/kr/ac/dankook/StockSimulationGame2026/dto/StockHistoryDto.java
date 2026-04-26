package kr.ac.dankook.StockSimulationGame2026.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockHistoryDto {
    private String date;
    private double open;
    private double high;
    private double low;
    private double close;
}
