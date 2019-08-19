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

ALTER TABLE p42_dat_user_states  ADD prev_login timestamp(0);
ALTER TABLE p42_dat_user_states  ADD prev_login_by_password timestamp(0);
ALTER TABLE p42_dat_user_states  ADD prev_login_by_api_key timestamp(0);
ALTER TABLE p42_dat_user_states  ADD prev_login_by_x509 timestamp(0);
