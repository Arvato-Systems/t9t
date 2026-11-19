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

import { RpcError, toRpcError, mapHttpErrorToRpcError } from './rpc-error';

describe('RpcError', () => {
  it('should_carry_return_code_message_details_and_http_status', () => {
    const err = new RpcError({
      returnCode: 800020097,
      errorMessage: 'Null pointer exception (see logs)',
      errorDetails: 'some details',
      httpStatus: 200,
    });

    expect(err).toBeInstanceOf(Error);
    expect(err.name).toBe('RpcError');
    expect(err.message).toBe('Null pointer exception (see logs)');
    expect(err.returnCode).toBe(800020097);
    expect(err.errorMessage).toBe('Null pointer exception (see logs)');
    expect(err.errorDetails).toBe('some details');
    expect(err.httpStatus).toBe(200);
  });

  it('should_default_error_details_and_http_status_to_null', () => {
    const err = new RpcError({ returnCode: 1, errorMessage: 'boom' });
    expect(err.errorDetails).toBeNull();
    expect(err.httpStatus).toBeNull();
  });
});

describe('toRpcError', () => {
  it('should_map_a_non_zero_return_code_response_to_rpc_error', () => {
    const err = toRpcError({
      returnCode: 800020122,
      errorMessage: 'Bad class passed to key resolver (must be final)',
      errorDetails: 't9t.auth.RoleKey',
    });

    expect(err).toBeInstanceOf(RpcError);
    expect(err.returnCode).toBe(800020122);
    expect(err.errorMessage).toBe('Bad class passed to key resolver (must be final)');
    expect(err.errorDetails).toBe('t9t.auth.RoleKey');
  });

  it('should_synthesize_a_message_when_error_message_is_missing', () => {
    const err = toRpcError({ returnCode: 42 });
    expect(err.errorMessage).toBe('RPC call failed with return code 42');
  });
});

describe('mapHttpErrorToRpcError', () => {
  it('should_extract_the_status_line_message_from_an_http_400', () => {
    const err = mapHttpErrorToRpcError({
      status: 400,
      statusText: 'Code 200017012 @ RoleCrudRequest.onlyActive: required',
      message:
        'Http failure response for https://example.test/rpc: 400 Code 200017012 @ RoleCrudRequest.onlyActive: required',
    });

    expect(err).toBeInstanceOf(RpcError);
    expect(err.returnCode).toBe(200017012);
    expect(err.errorMessage).toBe('Code 200017012 @ RoleCrudRequest.onlyActive: required');
    expect(err.httpStatus).toBe(400);
  });

  it('should_fall_back_to_status_text_when_the_message_marker_is_absent', () => {
    const err = mapHttpErrorToRpcError({
      status: 500,
      statusText: 'Internal Server Error',
      message: 'Http failure response for https://example.test/rpc: 500 Internal Server Error',
    });

    expect(err.httpStatus).toBe(500);
    expect(err.returnCode).toBe(0);
    expect(err.errorMessage).toBe('Internal Server Error');
  });

  it('should_prefer_a_service_response_body_on_a_non_2xx_status', () => {
    const err = mapHttpErrorToRpcError({
      status: 400,
      statusText: 'Bad Request',
      message: 'Http failure response for https://example.test/rpc: 400 Bad Request',
      error: {
        returnCode: 800020097,
        errorMessage: 'Null pointer exception (see logs)',
        errorDetails: 'details',
      },
    });

    expect(err.returnCode).toBe(800020097);
    expect(err.errorMessage).toBe('Null pointer exception (see logs)');
    expect(err.errorDetails).toBe('details');
    expect(err.httpStatus).toBe(400);
  });

  it('should_map_a_plain_error_to_rpc_error', () => {
    const err = mapHttpErrorToRpcError(new Error('network down'));
    expect(err).toBeInstanceOf(RpcError);
    expect(err.returnCode).toBe(0);
    expect(err.errorMessage).toBe('network down');
    expect(err.httpStatus).toBeNull();
  });

  it('should_map_an_unknown_value_to_rpc_error', () => {
    const err = mapHttpErrorToRpcError('something opaque');
    expect(err).toBeInstanceOf(RpcError);
    expect(err.errorMessage).toBe('RPC call failed');
  });
});
