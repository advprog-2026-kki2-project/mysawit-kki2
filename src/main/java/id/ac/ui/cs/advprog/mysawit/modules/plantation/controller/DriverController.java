package id.ac.ui.cs.advprog.mysawit.modules.plantation.controller;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Driver;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.service.DriverService;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.service.PlantationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final PlantationService plantationService;

    @GetMapping("/list")
    public String driverListPage(final Model model) {
        model.addAttribute("drivers", driverService.findAll());
        return "DriverList";
    }

    @GetMapping("/create")
    public String createDriverPage(final Model model) {
        model.addAttribute("driver", new Driver());
        return "CreateDriver";
    }

    @PostMapping("/create")
    public String createDriverPost(
            @ModelAttribute final Driver driver,
            final Model model) {
        try {
            driverService.create(driver);
            return "redirect:/driver/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("driver", driver);
            return "CreateDriver";
        }
    }

    @PostMapping("/delete/{driverId}")
    public String deleteDriver(@PathVariable final String driverId) {
        driverService.delete(driverId);
        return "redirect:/driver/list";
    }

    @GetMapping("/assign/{plantationId}")
    public String assignDriverPage(
            @PathVariable final String plantationId,
            final Model model) {
        final Plantation plantation = plantationService.findById(plantationId);
        final List<Driver> assigned = driverService.findByPlantation(plantationId);
        final List<Driver> all = driverService.findAll();
        all.removeIf(d -> plantation.getAssignedDriverIds().contains(d.getDriverId()));

        model.addAttribute("plantation", plantation);
        model.addAttribute("assignedDrivers", assigned);
        model.addAttribute("availableDrivers", all);
        return "AssignDriver";
    }

    @PostMapping("/assign/{plantationId}")
    public String assignDriverPost(
            @PathVariable final String plantationId,
            @RequestParam final String driverId,
            final Model model) {
        try {
            driverService.assignToPlantation(driverId, plantationId);
            return "redirect:/driver/assign/" + plantationId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/driver/assign/" + plantationId;
        }
    }

    @PostMapping("/unassign/{plantationId}/{driverId}")
    public String unassignDriver(
            @PathVariable final String plantationId,
            @PathVariable final String driverId) {
        driverService.removeFromPlantation(driverId, plantationId);
        return "redirect:/driver/assign/" + plantationId;
    }

}
