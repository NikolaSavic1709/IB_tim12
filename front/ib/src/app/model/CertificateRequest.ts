export interface CertificateRequest {
    requestStatus : string,
    serialNumber : string,
    issuer : string;
    startDate : Date;
    endDate : Date;
    certificateStatus: string;
    type : string;
    email : string;
}
