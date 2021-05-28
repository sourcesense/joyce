/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.connectorcore.scheduling;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractScheduler {

    @Value("${quartz.job.schedule.repeat-interval}")
    private Integer repeatInterval;

    protected static final String REPEAT_SCAN = "repeatScan";

    protected <T extends Job> JobDetail scanningJob(String job, Class<T> clazz) {
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
