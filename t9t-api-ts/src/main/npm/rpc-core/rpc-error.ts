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

/**
 * Unified error type for all t9t RPC failures.
 *
 * The t9t server signals failure in two distinct forms (verified against a
 * live takko server, see PLAN_TYPED.md §4c):
 *
 * 1. **Validation / transport errors** — HTTP 4xx with an *empty body* and the
 *    human-readable message written into the HTTP **status line** (reason
 *    phrase), e.g. `Code 200017012 @ RoleCrudRequest.onlyActive: required`.
 *    The server does this in `IServiceModule.error()`.
 *
 * 2. **Business errors** — HTTP 200 with a `ServiceResponse` body whose
 *    `returnCode != 0`, carrying `errorMessage` and `errorDetails`, even when
 *    a different response type was expected.
 *
 * Both forms are normalised into a single {@link RpcError} so consumers never
 * depend on Angular's `HttpErrorResponse` or on which form the server used.
 */
export class RpcError extends Error {
  /** t9t return code. `0` when only an HTTP-level failure is known. */
  readonly returnCode: number;
  /** Human-readable error message (status line or `errorMessage`). */
  readonly errorMessage: string;
  /** Optional machine-readable details (`errorDetails`), if the server sent them. */
  readonly errorDetails: string | null;
  /** HTTP status of the failing request, or `null` for non-HTTP failures. */
  readonly httpStatus: number | null;

  constructor(options: {
    returnCode: number;
    errorMessage: string;
    errorDetails?: string | null;
    httpStatus?: number | null;
  }) {
    super(options.errorMessage);
    this.name = 'RpcError';
    this.returnCode = options.returnCode;
    this.errorMessage = options.errorMessage;
    this.errorDetails = options.errorDetails ?? null;
    this.httpStatus = options.httpStatus ?? null;
    // Restore the prototype chain (required when targeting ES5/ES2015 classes).
    Object.setPrototypeOf(this, RpcError.prototype);
  }
}

/**
 * Maps a `ServiceResponse`-shaped body with a non-zero `returnCode` (error
 * form 2) into an {@link RpcError}.
 */
export function toRpcError(res: {
  returnCode: number;
  errorMessage?: string | null;
  errorDetails?: string | null;
}): RpcError {
  return new RpcError({
    returnCode: res.returnCode,
    errorMessage: res.errorMessage ?? `RPC call failed with return code ${res.returnCode}`,
    errorDetails: res.errorDetails ?? null,
  });
}

/**
 * Maps a low-level HTTP failure (error form 1, and any other non-2xx) into an
 * {@link RpcError}. The message is recovered from the HTTP status line (reason
 * phrase), which is where the t9t server writes validation errors.
 */
export function mapHttpErrorToRpcError(error: unknown): RpcError {
  if (isHttpErrorLike(error)) {
    // Some servers return a ServiceResponse body even on a non-2xx status;
    // prefer its structured fields when present.
    let body: unknown = error.error;
    if (typeof body === 'string') {
      try {
        body = JSON.parse(body) as unknown;
      } catch {
        body = null;
      }
    }
    if (body && typeof body === 'object') {
      const serviceBody = body as {
        returnCode?: unknown;
        errorMessage?: string | null;
        errorDetails?: string | null;
      };
      if (typeof serviceBody.returnCode === 'number') {
      return new RpcError({
        returnCode: serviceBody.returnCode,
        errorMessage: serviceBody.errorMessage ?? extractStatusLineMessage(error),
        errorDetails: serviceBody.errorDetails ?? null,
        httpStatus: error.status ?? null,
      });
      }
    }

    const message = extractStatusLineMessage(error);
    const codeMatch = /Code\s+(\d+)/.exec(message);
    return new RpcError({
      returnCode: codeMatch ? Number(codeMatch[1]) : 0,
      errorMessage: message || `HTTP ${error.status} error`,
      errorDetails: null,
      httpStatus: error.status ?? null,
    });
  }

  return new RpcError({
    returnCode: 0,
    errorMessage: error instanceof Error ? error.message : 'RPC call failed',
    errorDetails: null,
    httpStatus: null,
  });
}

function isHttpErrorLike(
  error: unknown,
): error is { status?: number; statusText?: string; message?: string; error?: unknown } {
  return typeof error === 'object' && error !== null && 'status' in error;
}

/**
 * Recovers the server's status-line message from an `HttpErrorResponse`.
 *
 * Angular's `HttpErrorResponse.message` has the form
 * `Http failure response for <url>: <status> <statusText>`, so the status-line
 * message is the part after `: <status> `. We fall back to `statusText` and
 * then to the raw `message` when the marker is not present.
 */
function extractStatusLineMessage(error: {
  status?: number;
  statusText?: string;
  message?: string;
}): string {
  const message = error.message ?? '';
  const status = error.status;
  if (status != null) {
    const marker = `: ${status} `;
    const idx = message.lastIndexOf(marker);
    if (idx >= 0) {
      const rest = message.slice(idx + marker.length).trim();
      if (rest.length > 0) {
        return rest;
      }
    }
  }
  const statusText = error.statusText?.trim();
  if (statusText && statusText.length > 0) {
    return statusText;
  }
  return message;
}
