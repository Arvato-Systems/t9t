///
/// Copyright (c) 2012 - 2026 Arvato Systems GmbH
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import {
  unwrapSearch,
  unwrapSearchWithTracking,
  unwrapCrud,
  unwrapCrudNullable,
  unwrapVoid,
  unwrapService,
  unwrapLean,
  unwrapResolve,
  unwrapResolveMany,
} from './unwrap';
import { RpcError } from './rpc-error';

describe('unwrapSearch', () => {
  it('should_return_the_data_payloads_from_data_list', () => {
    const res = {
      returnCode: 0,
      dataList: [
        { '@PQON': 'api.DataWithTrackingS', data: { roleId: 1 }, tracking: { version: 1 }, tenantId: 'TAKKO' },
        { '@PQON': 'api.DataWithTrackingS', data: { roleId: 2 }, tracking: { version: 2 }, tenantId: 'TAKKO' },
      ],
    };
    expect(unwrapSearch(res)).toEqual([{ roleId: 1 }, { roleId: 2 }]);
  });

  it('should_return_an_empty_array_for_an_empty_data_list', () => {
    expect(unwrapSearch({ returnCode: 0, dataList: [] })).toEqual([]);
  });

  it('should_return_an_empty_array_when_data_list_is_null', () => {
    expect(unwrapSearch({ returnCode: 0, dataList: null })).toEqual([]);
  });

  it('should_skip_null_entries', () => {
    const res = {
      returnCode: 0,
      dataList: [
        { data: { roleId: 1 } },
        null,
        { data: null },
        { data: { roleId: 3 } },
      ],
    };
    expect(unwrapSearch(res)).toEqual([{ roleId: 1 }, { roleId: 3 }]);
  });

  it('should_throw_an_rpc_error_on_a_non_zero_return_code', () => {
    expect(() =>
      unwrapSearch({
        returnCode: 800020097,
        errorMessage: 'Null pointer exception (see logs)',
        dataList: [],
      }),
    ).toThrow(RpcError);
  });
});

describe('unwrapSearchWithTracking', () => {
  it('should_return_data_and_tracking_pairs', () => {
    const res = {
      returnCode: 0,
      dataList: [{ data: { roleId: 1 }, tracking: { version: 7 } }],
    };
    expect(unwrapSearchWithTracking(res)).toEqual([{ data: { roleId: 1 }, tracking: { version: 7 } }]);
  });

  it('should_drop_entries_missing_data_or_tracking', () => {
    const res = {
      returnCode: 0,
      dataList: [
        { data: { roleId: 1 }, tracking: { version: 7 } },
        { data: null, tracking: { version: 8 } },
        { data: { roleId: 3 }, tracking: null },
      ],
    };
    expect(unwrapSearchWithTracking(res)).toEqual([{ data: { roleId: 1 }, tracking: { version: 7 } }]);
  });
});

describe('unwrapCrud', () => {
  it('should_return_the_data_payload', () => {
    expect(unwrapCrud({ returnCode: 0, data: { roleId: 1004 } })).toEqual({ roleId: 1004 });
  });

  it('should_throw_when_data_is_missing', () => {
    expect(() => unwrapCrud({ returnCode: 0, data: null })).toThrow('CRUD response missing data');
  });

  it('should_throw_an_rpc_error_on_a_non_zero_return_code', () => {
    expect(() => unwrapCrud({ returnCode: 800020122, errorMessage: 'Bad class', data: null })).toThrow(RpcError);
  });
});

describe('unwrapCrudNullable', () => {
  it('should_return_null_when_data_is_missing', () => {
    expect(unwrapCrudNullable({ returnCode: 0, data: null })).toBeNull();
  });

  it('should_return_the_data_payload_when_present', () => {
    expect(unwrapCrudNullable({ returnCode: 0, data: { roleId: 1 } })).toEqual({ roleId: 1 });
  });
});

describe('unwrapVoid', () => {
  it('should_return_undefined_on_success', () => {
    expect(unwrapVoid({ returnCode: 0 })).toBeUndefined();
  });

  it('should_throw_an_rpc_error_on_a_non_zero_return_code', () => {
    expect(() => unwrapVoid({ returnCode: 1, errorMessage: 'failed' })).toThrow(RpcError);
  });
});

describe('unwrapService', () => {
  it('should_return_the_response_with_return_code_stripped', () => {
    const res = { returnCode: 0, tenantId: 'TAKKO', processRef: 42, messageId: 'm1' };
    expect(unwrapService(res)).toEqual({ tenantId: 'TAKKO', processRef: 42, messageId: 'm1' });
  });

  it('should_throw_an_rpc_error_on_a_non_zero_return_code', () => {
    expect(() => unwrapService({ returnCode: 5, errorMessage: 'nope' })).toThrow(RpcError);
  });
});

describe('unwrapLean', () => {
  it('should_return_the_descriptions', () => {
    const res = {
      returnCode: 0,
      descriptions: [
        { objectRef: 1, id: 'a', name: 'A', isActive: true, differentTenant: false },
        null,
        { objectRef: 2, id: 'b', name: 'B', isActive: false, differentTenant: true },
      ],
    };
    expect(unwrapLean(res)).toHaveLength(2);
    expect(unwrapLean(res)[0]).toEqual({ objectRef: 1, id: 'a', name: 'A', isActive: true, differentTenant: false });
  });

  it('should_return_an_empty_array_when_descriptions_is_null', () => {
    expect(unwrapLean({ returnCode: 0, descriptions: null })).toEqual([]);
  });
});

describe('unwrapResolve', () => {
  it('should_return_the_key', () => {
    expect(unwrapResolve({ returnCode: 0, key: 5002202 })).toBe(5002202);
  });

  it('should_throw_when_key_is_missing', () => {
    expect(() => unwrapResolve({ returnCode: 0, key: null })).toThrow('Resolver response missing key');
  });
});

describe('unwrapResolveMany', () => {
  it('should_return_the_refs', () => {
    expect(unwrapResolveMany({ returnCode: 0, refs: [1, 2, 3] })).toEqual([1, 2, 3]);
  });

  it('should_drop_null_refs', () => {
    expect(unwrapResolveMany({ returnCode: 0, refs: [1, null, 3] })).toEqual([1, 3]);
  });

  it('should_return_an_empty_array_when_refs_is_null', () => {
    expect(unwrapResolveMany({ returnCode: 0, refs: null })).toEqual([]);
  });
});
