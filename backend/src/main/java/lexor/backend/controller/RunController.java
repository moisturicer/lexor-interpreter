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

        try {
            String output = LexorRunner.run(code, input);
            return ResponseEntity.ok(new RunResponse(output, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RunResponse(null, e.getMessage()));
        }
    }
}

