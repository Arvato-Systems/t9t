-- TBE-241 add response data sink

ALTER TABLE p42_cfg_data_sinks ADD response_data_sink_ref number(18);
ALTER TABLE p42_his_data_sinks ADD response_data_sink_ref number(18);
