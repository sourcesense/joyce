package com.sourcesense.nile.connectorcore.scheduling;

import com.sourcesense.nile.connectorcore.model.MappingInfo;
import com.sourcesense.nile.connectorcore.model.ProcessableData;
import com.sourcesense.nile.connectorcore.scheduling.job.ScanningJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractScheduler {

    @Value("${quartz.job.schedule.repeat-interval}")
    private Integer repeatInterval;

    protected static final String REPEAT_SCAN = "repeatScan";

    protected JobDetail scanningJob(String job, Class<? extends ScanningJob<? extends MappingInfo, ? extends ProcessableData>> clazz) {
        return JobBuilder.newJob(clazz)
                .withIdentity(job)
                .build();
    }

    protected Trigger repeatScanTrigger(String job) {
        return TriggerBuilder.newTrigger()
                .withIdentity(REPEAT_SCAN)
                .startNow()
                .withSchedule(this.ftpScanningSchedule())
                .forJob(job)
                .build();
    }

    private SimpleScheduleBuilder ftpScanningSchedule() {
        return SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(repeatInterval)
                .repeatForever();
    }
}
