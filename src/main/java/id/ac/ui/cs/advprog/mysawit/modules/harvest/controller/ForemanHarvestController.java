package id.ac.ui.cs.advprog.mysawit.modules.harvest.controller;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.AuthService;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.HarvestReviewRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.service.DailyHarvestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/foreman/harvests")
public class ForemanHarvestController {

    private final DailyHarvestService dailyHarvestService;
    private final AuthService authService;

    public ForemanHarvestController(DailyHarvestService dailyHarvestService, AuthService authService) {
        this.dailyHarvestService = dailyHarvestService;
        this.authService = authService;
    }

    @GetMapping
    public String listHarvests(@RequestParam(required = false) String laborerName,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestDate,
                               Authentication authentication,
                               Model model) {
        requireForeman(authentication);

        List<DailyHarvest> harvests = dailyHarvestService.getAllHarvests(laborerName, harvestDate);

        model.addAttribute("harvests", harvests);
        model.addAttribute("laborerName", laborerName);
        model.addAttribute("harvestDate", harvestDate);

        return "foreman-harvest-list";
    }

    @GetMapping("/{id}")
    public String harvestDetail(@PathVariable String id,
                                Authentication authentication,
                                Model model) {
        requireForeman(authentication);

        DailyHarvest harvest = dailyHarvestService.getHarvestById(id);

        model.addAttribute("harvest", harvest);
        model.addAttribute("reviewRequest", new HarvestReviewRequestDto());

        return "foreman-harvest-detail";
    }

    @PostMapping("/{id}/approve")
    public String approveHarvest(@PathVariable String id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        AuthResponse session = requireForeman(authentication);

        dailyHarvestService.approveHarvest(id, session.getUsername());
        redirectAttributes.addFlashAttribute("message", "Harvest approved successfully.");

        return "redirect:/foreman/harvests/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectHarvest(@PathVariable String id,
                                @ModelAttribute("reviewRequest") HarvestReviewRequestDto reviewRequest,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        AuthResponse session = requireForeman(authentication);

        try {
            dailyHarvestService.rejectHarvest(id, session.getUsername(), reviewRequest.getReason());
            redirectAttributes.addFlashAttribute("message", "Harvest rejected successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/foreman/harvests/" + id;
    }

    private AuthResponse requireForeman(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        AuthResponse session = authService.currentSession(authentication.getName());

        if (session.getRole() != Role.FOREMAN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return session;
    }
}