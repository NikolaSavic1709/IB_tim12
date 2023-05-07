export interface LoginRequest {
    email?: string | null;
    password?: string | null;
    mfaType?: string | null;
  }

  export interface LoginMFARequest {
    email?: string | null;
    password?: string | null;
    token?: number| null;
  }