package id.ac.ui.cs.advprog.mysawit.modules.plantation.controller;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Foreman;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.service.ForemanService;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.service.PlantationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/foreman")
@RequiredArgsConstructor
public class ForemanController {

    private final ForemanService foremanService;
    private final PlantationService plantationService;

    @GetMapping("/list")
    public String foremanListPage(final Model model) {
        model.addAttribute("foremen", foremanService.findAll());
        return "ForemanList";
    }

    @GetMapping("/create")
    public String createForemanPage(final Model model) {
        model.addAttribute("foreman", new Foreman());
        return "CreateForeman";
    }

    @PostMapping("/create")
    public String createForemanPost(
            @ModelAttribute final Foreman foreman,
            final Model model) {
        try {
            foremanService.create(foreman);
            return "redirect:/foreman/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("foreman", foreman);
            return "CreateForeman";
        }
    }

    @PostMapping("/delete/{foremanId}")
    public String deleteForeman(@PathVariable final String foremanId) {
        foremanService.delete(foremanId);
        return "redirect:/foreman/list";
    }

    @GetMapping("/assign/{plantationId}")
    public String assignForemanPage(
            @PathVariable final String plantationId,
            final Model model) {
        final Plantation plantation = plantationService.findById(plantationId);
        final List<Foreman> assigned = foremanService.findByPlantation(plantationId);
        final List<Foreman> all = foremanService.findAll();
        // Only show foremen not yet assigned
        all.removeIf(f -> plantation.getAssignedForemanIds().contains(f.getForemanId()));

        model.addAttribute("plantation", plantation);
        model.addAttribute("assignedForemen", assigned);
        model.addAttribute("availableForemen", all);
        return "AssignForeman";
    }

    @PostMapping("/assign/{plantationId}")
    public String assignForemanPost(
            @PathVariable final String plantationId,
            @RequestParam final String foremanId,
            final Model model) {
        try {
            foremanService.assignToPlantation(foremanId, plantationId);
            return "redirect:/foreman/assign/" + plantationId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/foreman/assign/" + plantationId;
        }
    }

    @PostMapping("/unassign/{plantationId}/{foremanId}")
    public String unassignForeman(
            @PathVariable final String plantationId,
            @PathVariable final String foremanId) {
        foremanService.removeFromPlantation(foremanId, plantationId);
        return "redirect:/foreman/assign/" + plantationId;
    }

}
