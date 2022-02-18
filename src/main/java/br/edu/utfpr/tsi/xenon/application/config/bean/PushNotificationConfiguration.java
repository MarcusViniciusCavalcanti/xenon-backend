package br.edu.utfpr.tsi.xenon.application.config.bean;

import br.edu.utfpr.tsi.xenon.domain.notification.worker.NotifyCarWaitingDecision;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class PushNotificationConfiguration implements SchedulingConfigurer {

    private final NotifyCarWaitingDecision notifyCarWaitingDecision;

    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
            notifyCarWaitingDecision::tick,
            context -> {
                var lastCompletionTime =
                    Optional.ofNullable(context.lastCompletionTime());
                var nextExecutionTime = lastCompletionTime.orElseGet(Date::new)
                    .toInstant().plus(NotifyCarWaitingDecision.DELAY, ChronoUnit.MINUTES);
                return Date.from(nextExecutionTime);
            }
        );
    }
}
