package trials.java.spring.nobelwinners.dashboard.demo.data;

import org.springframework.batch.item.ItemProcessor;

public class RawWinnerProcessor implements ItemProcessor<RawWinner, RawWinner> {

    @Override
    public RawWinner process(final RawWinner rawWinner) throws Exception {
        return rawWinner;
    }

}