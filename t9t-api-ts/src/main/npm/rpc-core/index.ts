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

export type { BonaPortable } from '@arvato-systems-jacs/bonaparte-typescript';
export { RpcError, toRpcError, mapHttpErrorToRpcError } from './rpc-error';
export { T9T_API_CONFIG, provideT9tApi } from './provide-t9t-api';
export type { T9tApiConfig } from './provide-t9t-api';
export { RpcClient } from './rpc-client';
export {
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
export type { ServiceResponseShape } from './unwrap';
export { parseRpcResponse, largeLongReviver } from './large-long';
export type { LargeLongOptions } from './large-long';
