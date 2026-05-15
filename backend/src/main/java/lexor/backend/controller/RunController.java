package lexor.backend.controller;

import lexor.backend.model.RunRequest;
import lexor.backend.model.RunResponse;
import lexor.core.LexorRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RunController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("LEXOR backend is running");
    }

    @PostMapping("/run")
    public ResponseEntity<RunResponse> run(@RequestBody RunRequest request) {
        String code  = request.getCode()  != null ? request.getCode()  : "";
        String input = request.getInput() != null ? request.getInput() : "";

        String result = LexorRunner.run(code, input);

        if (result.startsWith("error:")) {
            return ResponseEntity.ok(new RunResponse(null, result));
        } else {
            return ResponseEntity.ok(new RunResponse(result, null));
        }
    }
}