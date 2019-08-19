--
-- Copyright (c) 2012 - 2018 Arvato Systems GmbH
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

ALTER TABLE p42_cfg_data_sinks ADD (
    check_duplicate_filename number(1)
);
COMMENT ON COLUMN p42_cfg_data_sinks.check_duplicate_filename IS 'reject sending/receiving duplicate filenames on tenant level';


ALTER TABLE p42_his_data_sinks ADD (
    check_duplicate_filename number(1)
);
COMMENT ON COLUMN p42_his_data_sinks.check_duplicate_filename IS 'reject sending/receiving duplicate filenames on tenant level';


CREATE INDEX p42_dat_sinks_i3 ON p42_dat_sinks(
    file_or_queue_name, tenant_ref
) TABLESPACE rts42dat0I;
