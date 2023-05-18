export interface CreateCertificate {
    signatureAlgorithm : string,
    issuer : string | null,
    type : string,
    email : string;
}