export interface CertificateResponse {
    startDate: string,
    endDate: string,
    type: string,
    email: string;
}

export interface CertificatePage {
    totalCount: number,
    results: CertificateResponse[];
}
