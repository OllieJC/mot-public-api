package uk.gov.dvsa.mot.trade.api.response.mapper.cvsvehicle;

import org.junit.Before;
import org.junit.Test;

import uk.gov.dvsa.mot.trade.api.response.cvsvehicle.AnnualTestResponse;
import uk.gov.dvsa.mot.trade.api.response.cvsvehicle.AnnualTestV1Response;
import uk.gov.dvsa.mot.trade.api.response.cvsvehicle.CvsVehicleResponse;
import uk.gov.dvsa.mot.trade.api.response.cvsvehicle.CvsVehicleV1Response;
import uk.gov.dvsa.mot.trade.api.response.cvsvehicle.DefectResponse;
import uk.gov.dvsa.mot.trade.api.response.cvsvehicle.DefectV1Response;
import uk.gov.dvsa.mot.trade.api.response.mapper.MockVehicleDataHelper;
import uk.gov.dvsa.mot.vehicle.hgv.model.Defect;
import uk.gov.dvsa.mot.vehicle.hgv.model.TestHistory;
import uk.gov.dvsa.mot.vehicle.hgv.model.Vehicle;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

public class CvsVehicleV6ResponseMapperTest {

    private CvsVehicleV6ResponseMapper vehicleResponseMapper;

    @Before
    public void init() {
        vehicleResponseMapper = new CvsVehicleV6ResponseMapper();
    }

    @Test
    public void map_mapsAllPropertiesCorrectly() {
        List<Vehicle> vehiclesFromDb = Arrays.asList(
                MockVehicleDataHelper.getCvsVehicle("HGV", true),
                MockVehicleDataHelper.getCvsVehicle("PSV", true),
                MockVehicleDataHelper.getCvsVehicle("Trailer", false)
        );

        List<CvsVehicleResponse> mappedVehicles = vehicleResponseMapper.map(vehiclesFromDb);

        assertVehiclesMapped(vehiclesFromDb, mappedVehicles);
    }

    @Test
    public void ensureOnlyValidTestTypesAreMapped() {

        List<Vehicle> vehicleList = Arrays.asList(
                MockVehicleDataHelper.getCvsVehicleContainingSpecificTestType(
                        "HGV", Arrays.asList("INVALIDTYPE", "ANNUAL MV")),
                MockVehicleDataHelper.getCvsVehicleContainingSpecificTestType(
                        "PSV", Arrays.asList("PAID RETEST", "1ST PAID RETEST", "NOPE", "ANNUAL PSV SMALL")),
                MockVehicleDataHelper.getCvsVehicleContainingSpecificTestType(
                        "Trailer", Arrays.asList("TRI AXLE FREE RETEST", "ANNUAL TRAILER"))
        );

        List<CvsVehicleResponse> mappedVehicles = vehicleResponseMapper.map(vehicleList);

        assertEquals("Defined vehicle list size does not match response vehicle list size",
                vehicleList.size(),
                mappedVehicles.size()
        );

        // Assert we get the correct first vehicle of type HGV
        CvsVehicleResponse vehicleResponse = mappedVehicles.stream()
                .filter(v -> v.getVehicleType().equals("HGV"))
                .findFirst()
                .orElse(null);

        assertNotNull("Expected vehicle type of HGV in response vehicle list",
                vehicleResponse
        );

        assertEquals("Unexpected number of tests in HGV response test history",
                1,
                vehicleResponse.getAnnualTests().size()
        );

        assertTrue(
                "Failed asserting that HGV response vehicle contained ANNUAL MV",
                vehicleResponse
                        .getAnnualTests()
                        .stream()
                        .anyMatch(t -> t.getTestType()
                                .equals("ANNUAL MV")
                        )
        );

        assertTrue(
                "Failed asserting that HGV response vehicle did NOT contain INVALIDTYPE",
                vehicleResponse
                        .getAnnualTests()
                        .stream()
                        .noneMatch(t -> t.getTestType()
                                .equals("INVALIDTYPE")
                        )
        );

        // Assert we get the correct first vehicle of type PSV
        vehicleResponse = mappedVehicles.stream()
                .filter(v -> v.getVehicleType().equals("PSV"))
                .findFirst()
                .orElse(null);

        assertNotNull("Expected vehicle type of PSV in response vehicle list",
                vehicleResponse
        );

        assertEquals("Unexpected number of tests in PSV response test history",
                3,
                vehicleResponse.getAnnualTests().size()
        );

        assertTrue(
                "Failed asserting that PSV response vehicle contained PAID RETEST",
                vehicleResponse
                        .getAnnualTests()
                        .stream()
                        .anyMatch(t -> t.getTestType()
                                .equals("PAID RETEST")
                        )
        );

        assertTrue(
                "Failed asserting that PSV response vehicle contained 1ST PAID RETEST",
                vehicleResponse
                        .getAnnualTests()
                        .stream()
                        .anyMatch(t -> t.getTestType()
                                .equals("1ST PAID RETEST")
                        )
        );

        assertTrue(
                "Failed asserting that PSV response vehicle contained ANNUAL PSV SMALL",
                vehicleResponse
                        .getAnnualTests()
                        .stream()
                        .anyMatch(t -> t.getTestType()
                                .equals("ANNUAL PSV SMALL")
                        )
        );

        assertTrue(
                "Failed asserting that PSV response vehicle NOT contain INVALIDTYPE",
                vehicleResponse
                        .getAnnualTests()
                        .stream()
                        .noneMatch(t -> t.getTestType()
                                .equals("NOPE")
                        )
        );

        // Assert we get the correct first vehicle of type PSV
        vehicleResponse = mappedVehicles.stream()
                .filter(v -> v.getVehicleType().equals("Trailer"))
                .findFirst()
                .orElse(null);

        assertNotNull("Expected vehicle type of PSV in response vehicle list",
                vehicleResponse
        );

        assertEquals("Unexpected number of tests in PSV response test history",
                2,
                vehicleResponse.getAnnualTests().size()
        );

        assertTrue(
                "Failed asserting that Trailer response vehicle contained TRI AXLE FREE RETEST",
                vehicleResponse
                        .getAnnualTests()
                        .stream()
                        .anyMatch(t -> t.getTestType()
                                .equals("TRI AXLE FREE RETEST")
                        )
        );

        assertTrue(
                "Failed asserting that Trailer response vehicle contained ANNUAL TRAILER",
                vehicleResponse
                        .getAnnualTests()
                        .stream()
                        .anyMatch(t -> t.getTestType()
                                .equals("ANNUAL TRAILER")
                        )
        );
    }

    private void assertVehiclesMapped(List<Vehicle> vehicles, List<CvsVehicleResponse> mappedVehicles) {
        assertEquals(vehicles.size(), mappedVehicles.size());

        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle vehicle = vehicles.get(i);
            assertEquals(CvsVehicleV1Response.class, mappedVehicles.get(i).getClass());
            CvsVehicleV1Response responseVehicle = (CvsVehicleV1Response) mappedVehicles.get(i);

            assertEquals(vehicle.getVehicleIdentifier(), responseVehicle.getRegistration());
            assertEquals(vehicle.getMake(), responseVehicle.getMake());
            assertEquals(vehicle.getModel(), responseVehicle.getModel());
            assertEquals(vehicle.getVehicleType(), responseVehicle.getVehicleType());
            assertEquals(vehicle.getVehicleClass(), responseVehicle.getVehicleClass());
            assertEquals(
                    vehicleResponseMapper.transformDate(vehicle.getRegistrationDate()),
                    responseVehicle.getRegistrationDate()
            );
            assertEquals(
                    vehicleResponseMapper.transformDate(vehicle.getManufactureDate()),
                    responseVehicle.getManufactureDate()
            );
            assertEquals(
                    vehicleResponseMapper.transformDate(vehicle.getTestCertificateExpiryDate()),
                    responseVehicle.getAnnualTestExpiryDate()
            );
            assertAnnualTestMapped(vehicle.getTestHistory(), responseVehicle.getAnnualTests());
        }
    }

    private void assertAnnualTestMapped(List<TestHistory> annualTests, List<AnnualTestResponse> mappedAnnualTests) {
        if (annualTests == null) {
            assertNull(mappedAnnualTests);
            return;
        }
        assertEquals(annualTests.size(), mappedAnnualTests.size());

        for (int i = 0; i < annualTests.size(); i++) {
            TestHistory annualTest = annualTests.get(i);

            assertEquals(AnnualTestV1Response.class, mappedAnnualTests.get(i).getClass());
            AnnualTestV1Response responseTest = (AnnualTestV1Response) mappedAnnualTests.get(i);

            assertEquals(annualTest.getTestType(), responseTest.getTestType());
            assertEquals(
                    vehicleResponseMapper.transformDate(annualTest.getTestDate()),
                    responseTest.getTestDate()
            );
            assertEquals(annualTest.getTestResult(), responseTest.getTestResult());
            assertEquals(annualTest.getTestCertificateSerialNo(), responseTest.getTestCertificateNumber());
            assertEquals(
                    vehicleResponseMapper.transformDate(annualTest.getTestCertificateExpiryDateAtTest()),
                    responseTest.getExpiryDate()
            );
            assertEquals(annualTest.getNumberOfAdvisoryDefectsAtTest().toString(), responseTest.getNumberOfAdvisoryDefectsAtTest());
            assertEquals(annualTest.getNumberOfDefectsAtTest().toString(), responseTest.getNumberOfDefectsAtTest());

            assertDefectsMapped(annualTest.getTestHistoryDefects(), responseTest.getDefects());
        }
    }

    private void assertDefectsMapped(List<Defect> defects, List<DefectResponse> mappedDefects) {
        if (defects == null) {
            assertNull(mappedDefects);
            return;
        }
        assertEquals(defects.size(), mappedDefects.size());

        for (int i = 0; i < defects.size(); i++) {
            Defect defect = defects.get(i);

            assertEquals(DefectV1Response.class, mappedDefects.get(i).getClass());
            DefectV1Response responseDefect = (DefectV1Response) mappedDefects.get(i);

            assertEquals(defect.getFailureItemNo().toString(), responseDefect.getFailureItemNo());
            assertEquals(defect.getFailureReason(), responseDefect.getFailureReason());
            assertEquals(defect.getSeverityCode(), responseDefect.getSeverityCode());
            assertEquals(defect.getSeverityDescription(), responseDefect.getSeverityDescription());
        }
    }
}
