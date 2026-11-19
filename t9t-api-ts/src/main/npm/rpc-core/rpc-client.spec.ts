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

import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { RpcClient } from './rpc-client';
import { RpcError } from './rpc-error';
import { provideT9tApi } from './provide-t9t-api';
import { unwrapVoid } from './unwrap';

const RPC_URL = 'https://example.test/rpc';

describe('RpcClient', () => {
  let client: RpcClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideT9tApi({ rpcUrl: RPC_URL, largeLong: { largeLongFields: ['simpleHash'] } }),
      ],
    });

    client = TestBed.inject(RpcClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should_post_envelope_body_with_pqon_to_configured_rpc_url', () => {
    client.call('t9t.auth.request.RoleSearchRequest', { limit: 10, offset: 0 }).subscribe();

    const req = httpMock.expectOne(RPC_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      '@PQON': 't9t.auth.request.RoleSearchRequest',
      limit: 10,
      offset: 0,
    });

    req.flush('{"returnCode":0}');
  });

  it('should_emit_the_raw_response_body_on_success', () => {
    let captured: unknown;
    client
      .call<{ returnCode: number; data?: { roleId: number } }>('t9t.auth.request.RoleCrudRequest', {
        crud: 'R',
        key: 1004,
      })
      .subscribe({ next: (res) => (captured = res) });

    const req = httpMock.expectOne(RPC_URL);
    req.flush('{"returnCode":0,"data":{"@PQON":"t9t.auth.RoleDTO","roleId":1004}}');

    expect(captured).toEqual({ returnCode: 0, data: { '@PQON': 't9t.auth.RoleDTO', roleId: 1004 } });
  });

  // Error form 1: HTTP 400, empty body, message in the status line.
  it('should_map_http_400_with_status_line_message_to_rpc_error', () => {
    let capturedError: unknown;
    client.call('t9t.auth.request.RoleCrudRequest', { crud: 'R' }).subscribe({
      error: (error) => (capturedError = error),
    });

    const req = httpMock.expectOne(RPC_URL);
    req.flush(null, {
      status: 400,
      statusText: 'Code 200017012 @ RoleCrudRequest.onlyActive: required',
    });

    expect(capturedError).toBeInstanceOf(RpcError);
    const rpcError = capturedError as RpcError;
    expect(rpcError.returnCode).toBe(200017012);
    expect(rpcError.errorMessage).toBe('Code 200017012 @ RoleCrudRequest.onlyActive: required');
    expect(rpcError.httpStatus).toBe(400);
  });

  it('should_map_http_500_to_rpc_error_with_http_status', () => {
    let capturedError: unknown;
    client.call('t9t.auth.request.RoleCrudRequest', {}).subscribe({
      error: (error) => (capturedError = error),
    });

    const req = httpMock.expectOne(RPC_URL);
    req.flush(null, { status: 500, statusText: 'Internal Server Error' });

    expect(capturedError).toBeInstanceOf(RpcError);
    const rpcError = capturedError as RpcError;
    expect(rpcError.httpStatus).toBe(500);
    expect(rpcError.returnCode).toBe(0);
    expect(rpcError.errorMessage).toBe('Internal Server Error');
  });

  // Error form 2: HTTP 200 with a ServiceResponse body where returnCode != 0.
  // The client emits the raw body; the unwrap helper converts it to an RpcError.
  it('should_emit_a_200_body_with_non_zero_return_code_that_unwrap_maps_to_rpc_error', () => {
    let captured: unknown;
    client.call('t9t.auth.request.RoleCrudRequest', { crud: 'R', key: 1004 }).subscribe({
      next: (res) => (captured = res),
    });

    const req = httpMock.expectOne(RPC_URL);
    req.flush(
      '{"returnCode":800020097,"errorMessage":"Null pointer exception (see logs)","errorDetails":"t9t.auth.RoleKey"}',
    );

    // The client emits the raw 200 body (it does not inspect returnCode).
    expect(captured).toEqual({
      returnCode: 800020097,
      errorMessage: 'Null pointer exception (see logs)',
      errorDetails: 't9t.auth.RoleKey',
    });

    // The unwrap helper is what turns the non-zero returnCode into an RpcError.
    expect(() => unwrapVoid(captured as { returnCode: number })).toThrow(RpcError);
    expect(() => unwrapVoid(captured as { returnCode: number })).toThrow('Null pointer exception (see logs)');
  });

  it('should_post_envelope_body_with_an_explicit_bearer_token', () => {
    client.callWithToken('t9t.authc.api.ResetPasswordRequest', 'technical-jwt', { userId: 'jdoe' }).subscribe();

    const req = httpMock.expectOne(RPC_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer technical-jwt');
    expect(req.request.body).toEqual({
      '@PQON': 't9t.authc.api.ResetPasswordRequest',
      userId: 'jdoe',
    });

    req.flush('{"returnCode":0}');
  });

  it('should_map_http_error_to_rpc_error_for_call_with_token', () => {
    let capturedError: unknown;
    client.callWithToken('t9t.authc.api.ResetPasswordRequest', 'technical-jwt', {}).subscribe({
      error: (error) => (capturedError = error),
    });

    const req = httpMock.expectOne(RPC_URL);
    req.flush(null, { status: 400, statusText: 'Code 200017012 @ ResetPasswordRequest.userId: required' });

    expect(capturedError).toBeInstanceOf(RpcError);
    expect((capturedError as RpcError).returnCode).toBe(200017012);
  });

  it('should_preserve_configured_large_longs_for_regular_and_token_calls', () => {
    const captured: unknown[] = [];
    client.call('t9t.auth.request.RoleCrudRequest', {}).subscribe((res) => captured.push(res));
    client.callWithToken('t9t.auth.request.RoleCrudRequest', 'technical-jwt', {}).subscribe((res) => captured.push(res));

    const requests = httpMock.match(RPC_URL);
    expect(requests).toHaveLength(2);
    const tokenRequest = requests.find((request) => request.request.headers.has('Authorization'))!;
    const regularRequest = requests.find((request) => !request.request.headers.has('Authorization'))!;
    expect(regularRequest.request.responseType).toBe('text');
    expect(tokenRequest.request.responseType).toBe('text');
    expect(tokenRequest.request.headers.get('Authorization')).toBe('Bearer technical-jwt');

    regularRequest.flush('{"returnCode":0,"data":{"simpleHash":36601872016744006}}');
    tokenRequest.flush('{"returnCode":0,"data":{"simpleHash":36601872016744006}}');

    expect(captured).toEqual([
      { returnCode: 0, data: { simpleHash: '36601872016744006' } },
      { returnCode: 0, data: { simpleHash: '36601872016744006' } },
    ]);
  });

  it('should_map_a_textual_json_http_error_body_to_rpc_error', () => {
    let capturedError: unknown;
    client.call('t9t.auth.request.RoleCrudRequest', {}).subscribe({ error: (error) => (capturedError = error) });

    const req = httpMock.expectOne(RPC_URL);
    req.flush('{"returnCode":800020097,"errorMessage":"Request rejected","errorDetails":"details"}', {
      status: 400,
      statusText: 'Bad Request',
    });

    expect(capturedError).toBeInstanceOf(RpcError);
    expect(capturedError).toMatchObject({
      returnCode: 800020097,
      errorMessage: 'Request rejected',
      errorDetails: 'details',
      httpStatus: 400,
    });
  });
});

describe('RpcClient.loginRpc', () => {
  const LOGIN_URL = 'https://example.test/login';
  let client: RpcClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideT9tApi({ rpcUrl: RPC_URL, loginUrl: LOGIN_URL }),
      ],
    });
    client = TestBed.inject(RpcClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should_post_envelope_body_to_the_configured_login_url_without_bearer_token', () => {
    client.loginRpc('t9t.authc.api.LoginRequest', { userId: 'jdoe', password: 'secret' }).subscribe();

    const req = httpMock.expectOne(LOGIN_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.has('Authorization')).toBe(false);
    expect(req.request.body).toEqual({
      '@PQON': 't9t.authc.api.LoginRequest',
      userId: 'jdoe',
      password: 'secret',
    });

    req.flush('{"returnCode":0}');
  });

  it('should_fall_back_to_rpc_url_when_no_login_url_is_configured', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideT9tApi({ rpcUrl: RPC_URL })],
    });
    client = TestBed.inject(RpcClient);
    httpMock = TestBed.inject(HttpTestingController);

    client.loginRpc('t9t.authc.api.LoginRequest', {}).subscribe();

    const req = httpMock.expectOne(RPC_URL);
    expect(req.request.body).toEqual({ '@PQON': 't9t.authc.api.LoginRequest' });
    req.flush('{"returnCode":0}');
  });

  it('should_map_http_errors_to_rpc_error_for_login_rpc', () => {
    let capturedError: unknown;
    client.loginRpc('t9t.authc.api.LoginRequest', {}).subscribe({
      error: (error) => (capturedError = error),
    });

    const req = httpMock.expectOne(LOGIN_URL);
    req.flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(capturedError).toBeInstanceOf(RpcError);
    expect((capturedError as RpcError).httpStatus).toBe(401);
  });
});

describe('RpcClient without large-long configuration', () => {
  let client: RpcClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideT9tApi({ rpcUrl: RPC_URL })],
    });
    client = TestBed.inject(RpcClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should_keep_angular_json_response_parsing_when_no_long_fields_are_configured', () => {
    let captured: unknown;
    client.call('t9t.auth.request.RoleCrudRequest', {}).subscribe((res) => (captured = res));

    const req = httpMock.expectOne(RPC_URL);
    expect(req.request.responseType).toBe('json');
    req.flush({ returnCode: 0, data: { roleId: 1004 } });

    expect(captured).toEqual({ returnCode: 0, data: { roleId: 1004 } });
  });
});
