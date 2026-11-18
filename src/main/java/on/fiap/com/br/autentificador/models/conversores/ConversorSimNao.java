package on.fiap.com.br.autentificador.models.conversores;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converte Boolean para 'S' / 'N' (constraint ck_cargo).
 */
@Converter
public class ConversorSimNao implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean valor) {
        if (valor == null) {
            return null;
        }
        return valor ? "S" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        return "S".equals(valor.trim());
    }
}
