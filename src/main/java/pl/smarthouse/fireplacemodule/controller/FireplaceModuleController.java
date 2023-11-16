package pl.smarthouse.fireplacemodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.fireplacemodule.service.FireplaceModuleService;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleDto;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class FireplaceModuleController {
  private final FireplaceModuleService fireplaceModuleService;

  @GetMapping("/fireplace")
  public Mono<FireplaceModuleDto> getFireplaceModule() {
    return Mono.just(fireplaceModuleService.getFireplaceModule());
  }
}
