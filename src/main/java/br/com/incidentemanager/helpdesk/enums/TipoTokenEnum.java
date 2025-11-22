package br.com.incidentemanager.helpdesk.enums;

public enum TipoTokenEnum {
    CRIACAO_SENHA(
        "Este link expirou. Entre em contato com o administrador do sistema para solicitar um novo.",
        "Este link já foi utilizado."
    ),
    REDEFINICAO_SENHA(
        "Link expirado. Solicite um novo.",
        "Link já utilizado. Solicite um novo."
    );

    private final String msgExpirado;
    private final String msgUsado;

    TipoTokenEnum(String msgExpirado, String msgUsado) {
        this.msgExpirado = msgExpirado;
        this.msgUsado = msgUsado;
    }

    public String getMsgExpirado() { return msgExpirado; }
    public String getMsgUsado() { return msgUsado; }
}
