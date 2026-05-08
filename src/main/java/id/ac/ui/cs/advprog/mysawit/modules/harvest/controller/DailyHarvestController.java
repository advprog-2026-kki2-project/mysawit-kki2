package id.ac.ui.cs.advprog.mysawit.modules.harvest.controller;

import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.DailyHarvestRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.service.DailyHarvestService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DailyHarvestController {

    private final DailyHarvestService dailyHarvestService;

    public DailyHarvestController(DailyHarvestService dailyHarvestService) {
        this.dailyHarvestService = dailyHarvestService;
    }

    @GetMapping("/harvest/create")
    public String showForm(Model model) {
        model.addAttribute("harvest", new DailyHarvestRequestDto());
        return "harvest-form";
    }

    @PostMapping("/harvest/create")
    public String submitHarvest(@ModelAttribute("harvest") DailyHarvestRequestDto harvest,
                                @RequestParam("photo") MultipartFile photo,
                                Model model) {
        try {
            dailyHarvestService.recordHarvest(harvest, photo);
            model.addAttribute("message", "Harvest submitted successfully.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("harvest", new DailyHarvestRequestDto());
        return "harvest-form";
    }

    @GetMapping("/harvest/history")
    public String showLaborerHarvestHistory(
            @RequestParam(required = false) String laborerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        List<DailyHarvest> harvests = dailyHarvestService.getLaborerHarvestHistory(
                laborerName,
                status,
                startDate,
                endDate
        );

        model.addAttribute("harvests", harvests);
        model.addAttribute("laborerName", laborerName);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "laborer-harvest-history";
    }
}