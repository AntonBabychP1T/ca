package service.carsharing.mapper;

import org.mapstruct.Mapper;
import service.carsharing.config.MapperConfig;
import service.carsharing.dto.payment.PaymentResponseDto;
import service.carsharing.model.Payment;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    PaymentResponseDto toDto(Payment payment);
}
