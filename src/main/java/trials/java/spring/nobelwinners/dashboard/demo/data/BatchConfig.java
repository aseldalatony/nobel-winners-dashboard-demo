package trials.java.spring.nobelwinners.dashboard.demo.data;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import trials.java.spring.nobelwinners.dashboard.demo.model.Gender;
import trials.java.spring.nobelwinners.dashboard.demo.model.Winner;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public FlatFileItemReader<RawWinner> reader() {
        return new FlatFileItemReaderBuilder<RawWinner>()
                .name("winnerItemReader")
                .resource(new ClassPathResource("nobel_prize_by_winner.csv"))
                .delimited()
                .names("id", "firstname", "surname", "born", "died", "bornCountry", "bornCountryCode", "bornCity", "diedCountry", "diedCountryCode", "diedCity", "gender", "year", "category", "overallMotivation", "share", "motivation", "name", "city", "country")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<RawWinner>() {{
                    setTargetType(RawWinner.class);
                }})
                .build();
    }

    @Bean
    public JpaItemWriter<RawWinner> writer(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<RawWinner>().entityManagerFactory(entityManagerFactory).build();
    }

    @Bean
    public Step step1(JpaItemWriter<RawWinner> writer) {
        return stepBuilderFactory.get("step1")
                .<RawWinner, RawWinner>chunk(10)
                .reader(reader())
                .writer(writer)
                .build();
    }

    @Bean
    public Step step2(EntityManagerFactory entityManagerFactory) {
        return stepBuilderFactory.get("step2")
                .<Object[], Winner>chunk(10)
                .reader(new JpaCursorItemReaderBuilder<Object[]>()
                        .entityManagerFactory(entityManagerFactory)
                        .name("RawReader")
                        .queryString("select distinct r.firstname, r.surname, r.gender, r.born, r.bornCountry from RawWinner r")
                        .build())
                .processor(new ItemProcessor<Object[], Winner>() {
                    @Override
                    public Winner process(Object[] rawWinner) throws Exception {
                        Winner winner = new Winner();
                        winner.setFirstName((String) rawWinner[0]);
                        winner.setSurName((String) rawWinner[1]);
                        winner.setBirthCountry((String) rawWinner[4]);
                        winner.setBirthDate(getDate(rawWinner[3]));
                        switch ((String) rawWinner[2]) {
                            case "male":
                                winner.setGender(Gender.Male);
                                break;
                            case "female":
                                winner.setGender(Gender.Female);
                                break;
                            default:
                                winner.setGender(Gender.Organization);
                                break;
                        }
                        return winner;
                    }
                })
                .writer(new JpaItemWriterBuilder<Winner>().entityManagerFactory(entityManagerFactory).build())
                .build();
    }

    private LocalDate getDate(Object o) {
        try {
            return LocalDate.parse((CharSequence) o, DateTimeFormatter.ofPattern("M/d/yyyy"));
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public Job importUserJob(JobBuilderFactory jobBuilderFactory, Step step1, Step step2) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1).next(step2)
                .end()
                .build();
    }
}
