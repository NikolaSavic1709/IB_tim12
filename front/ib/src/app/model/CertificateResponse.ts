export interface CertificateResponse {
    startDate: string,
    endDate: string,
    type: string,
    email: string,
    serialNumber:string;
}

export interface CertificatePage {
    totalCount: number,
    results: CertificateResponse[];
}
