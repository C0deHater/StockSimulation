package kr.ac.dankook.StockSimulationGame2026;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling //스스로 주가를 업데이트 하기 위함
@SpringBootApplication
public class StockSimulationGame2026Application {

	public static void main(String[] args)
	{
		SpringApplication.run(StockSimulationGame2026Application.class, args);
	}

}
