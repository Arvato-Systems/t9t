--
-- Copyright (c) 2012 - 2025 Arvato Systems GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

ALTER TABLE qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_FK
    FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
    REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP);

ALTER TABLE qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_FK
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

ALTER TABLE qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_FK
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

ALTER TABLE qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_FK
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

ALTER TABLE qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_FK
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
