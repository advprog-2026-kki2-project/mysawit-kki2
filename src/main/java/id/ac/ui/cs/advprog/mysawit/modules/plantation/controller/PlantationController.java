package id.ac.ui.cs.advprog.mysawit.modules.plantation.controller;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Coordinate;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.service.PlantationService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/plantation")
@RequiredArgsConstructor
public class PlantationController {

    private final PlantationService service;

    @GetMapping("/create")
    public String createPlantationPage(final Model model) {
        final Plantation plantation = new Plantation();
        model.addAttribute("plantation", plantation);
        return "CreatePlantation";
    }

    @PostMapping("/create")
    public String createPlantationPost(
            @ModelAttribute Plantation plantation,
            @RequestParam("x0") final double x0, @RequestParam("y0") final double y0,
            @RequestParam("x1") final double x1, @RequestParam("y1") final double y1,
            @RequestParam("x2") final double x2, @RequestParam("y2") final double y2,
            @RequestParam("x3") final double x3, @RequestParam("y3") final double y3,
            final Model model) {
        try {
            plantation.setCorners(buildCorners(x0, y0, x1, y1, x2, y2, x3, y3));
            service.create(plantation);
            return "redirect:list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("plantation", plantation);
            return "CreatePlantation";
        }
    }

    @GetMapping("/list")
    public String plantationListPage(final Model model) {
        final List<Plantation> allPlantations = service.findAll();
        model.addAttribute("plantations", allPlantations);
        return "PlantationList";
    }

    @GetMapping("/edit/{plantationId}")
    public String editPlantationPage(
            @PathVariable("plantationId") final String plantationId, final Model model) {
        final Plantation plantation = service.findById(plantationId);
        model.addAttribute("plantation", plantation);
        return "EditPlantation";
    }

    @PostMapping("/update")
    public String editPlantationPost(
            @ModelAttribute Plantation plantation,
            @RequestParam("x0") final double x0, @RequestParam("y0") final double y0,
            @RequestParam("x1") final double x1, @RequestParam("y1") final double y1,
            @RequestParam("x2") final double x2, @RequestParam("y2") final double y2,
            @RequestParam("x3") final double x3, @RequestParam("y3") final double y3,
            final Model model) {
        try {
            plantation.setCorners(buildCorners(x0, y0, x1, y1, x2, y2, x3, y3));
            service.update(plantation.getPlantationId(), plantation);
            return "redirect:list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("plantation", plantation);
            return "EditPlantation";
        }
    }

    @PostMapping("/delete/{plantationId}")
    public String deletePlantation(@PathVariable("plantationId") final String plantationId) {
        service.delete(plantationId);
        return "redirect:/plantation/list";
    }

    private List<Coordinate> buildCorners(
            final double x0, final double y0,
            final double x1, final double y1,
            final double x2, final double y2,
            final double x3, final double y3) {
        final List<Coordinate> corners = new ArrayList<>();
        corners.add(makeCoord(x0, y0));
        corners.add(makeCoord(x1, y1));
        corners.add(makeCoord(x2, y2));
        corners.add(makeCoord(x3, y3));
        return corners;
    }

    private Coordinate makeCoord(final double x, final double y) {
        final Coordinate coord = new Coordinate();
        coord.setX(x);
        coord.setY(y);
        return coord;
    }

}
