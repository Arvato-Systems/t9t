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

ALTER TABLE p42_cfg_data_sinks ADD COLUMN xml_namespace_mappings varchar(255);
COMMENT ON COLUMN p42_cfg_data_sinks.xml_namespace_mappings IS 'xml name space mappings: <namespace-prefix>=<namespace-url> {(; NEWLINE) [namespace-prefix]=[namespace-url]}';


ALTER TABLE p42_his_data_sinks ADD COLUMN xml_namespace_mappings varchar(255);
COMMENT ON COLUMN p42_his_data_sinks.xml_namespace_mappings IS 'xml name space mappings: <namespace-prefix>=<namespace-url> {(; NEWLINE) [namespace-prefix]=[namespace-url]}';
