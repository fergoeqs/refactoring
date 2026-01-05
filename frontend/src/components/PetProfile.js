import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import useAxiosWithAuth from "../AxiosAuth";
import EditPetModal from "./EditPetModal";
import AddAnamnesisModal from "./AddAnamnesisModal";
import AddHealthUpdateModal from "./AddHealthUpdateModal";
import PetInfo from "./PetInfo";
import HealthUpdateDetailsModal from "./HealthUpdateDetailsModal";
import Header from "./Header";

const PetProfilePage = () => {
    const { petId } = useParams();
    const navigate = useNavigate();
    const axiosInstance = useAxiosWithAuth();
    const [petInfo, setPetInfo] = useState(null);
    const [anamneses, setAnamneses] = useState([]);
    const [healthUpdates, setHealthUpdates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isAddAnamnesisModalOpen, setIsAddAnamnesisModalOpen] = useState(false);
    const [isAddHealthUpdateModalOpen, setIsAddHealthUpdateModalOpen] = useState(false);
    const [isHealthUpdateDetailsModalOpen, setIsHealthUpdateDetailsModalOpen] = useState(false);
    const [selectedHealthUpdateId, setSelectedHealthUpdateId] = useState(null);
    const [upcomingAppointments, setUpcomingAppointments] = useState([]);
    const [treatments, setTreatments] = useState([]);
    const [userRole, setUserRole] = useState("");
    const [isChangeSlotModalOpen, setIsChangeSlotModalOpen] = useState(false);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [availableSlots, setAvailableSlots] = useState([]);
    const [newSlotId, setNewSlotId] = useState(null);
    const [procedures, setProcedures] = useState([]);

    const fetchData = async () => {
        setLoading(true);
        setError(null);

        try {
            const petResponse = await axiosInstance.get(`/pets/pet/${petId}`);
            setPetInfo(petResponse.data);

            const anamnesesResponse = await axiosInstance.get(`/anamnesis/all-by-patient/${petId}`);
            setAnamneses(anamnesesResponse.data);

            const healthUpdatesResponse = await axiosInstance.get(`/health/all/${petId}`);
            setHealthUpdates(healthUpdatesResponse.data);

            const appointmentsResponse = await axiosInstance.get(`/appointments/upcoming-pet/${petId}`);
            const appointmentsWithDetails = await Promise.all(
                appointmentsResponse.data.map(async (appointment) => {
                    const slotResponse = await axiosInstance.get(`/slots/${appointment.slotId}`);
                    const slotData = slotResponse.data;

                    const vetResponse = await axiosInstance.get(`/users/user-info/${slotData.vetId}`);
                    const vetName = `${vetResponse.data.name} ${vetResponse.data.surname}`;

                    return {
                        ...appointment,
                        slot: {
                            ...slotData,
                            vetName,
                        },
                    };
                })
            );
            setUpcomingAppointments(appointmentsWithDetails);

            const treatmentsResponse = await axiosInstance.get(`/treatments/actual-by-pet/${petId}`);
            setTreatments(treatmentsResponse.data);

            const proceduresResponse = await axiosInstance.get(`/procedures/all-by-pet/${petId}`);
            setProcedures(proceduresResponse.data);

            const userResponse = await axiosInstance.get("/users/current-user-info");
            setUserRole(userResponse.data.role);
        } catch (error) {
            console.error("Error fetching data:", error);
            setError("Failed to fetch data. Please try again later.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [petId, axiosInstance]);

    const handleSavePet = async (updatedData) => {
        try {
            await axiosInstance.put(`/pets/update-pet/${petId}`, updatedData);
            setIsEditModalOpen(false);
            fetchData();
        } catch (error) {
            console.error("Error updating pet info:", error);
        }
    };

    const handleSaveAnamnesis = async (anamnesisData) => {
        try {
            await axiosInstance.post("/anamnesis/save", {
                ...anamnesisData,
                pet: petId,
                appointment: anamnesisData.appointment,
            });
            setIsAddAnamnesisModalOpen(false);
            fetchData();
        } catch (error) {
            console.error("Error saving anamnesis:", error);
        }
    };

    const handleSaveHealthUpdate = async (healthUpdateData) => {
        try {
            await axiosInstance.post("/health/save", healthUpdateData);
            setIsAddHealthUpdateModalOpen(false);
            fetchData();
        } catch (error) {
            console.error("Error saving health update:", error);
        }
    };

    const handleViewHealthUpdateDetails = (id) => {
        setSelectedHealthUpdateId(id);
        setIsHealthUpdateDetailsModalOpen(true);
    };

    const openChangeSlotModal = (appointment) => {
        setSelectedAppointment(appointment);
        fetchAvailableSlots();
        setIsChangeSlotModalOpen(true);
    };

    const closeChangeSlotModal = () => {
        setIsChangeSlotModalOpen(false);
        setSelectedAppointment(null);
        setAvailableSlots([]);
        setNewSlotId(null);
    };

    const fetchAvailableSlots = async () => {
        try {
            const response = await axiosInstance.get("/slots/available-priority-slots");
            setAvailableSlots(response.data);
        } catch (error) {
            console.error("Error fetching available slots:", error);
        }
    };

    const handleChangeSlot = async () => {
        if (!newSlotId) {
            return;
        }

        try {
            await axiosInstance.put(`/slots/book-slot/${newSlotId}`);
            await axiosInstance.put(`/slots/release-slot/${selectedAppointment.slotId}`);
            await axiosInstance.put(`/appointments/update-appointment/${selectedAppointment.id}`, null, {
                params: { slotId: newSlotId },
            });
            closeChangeSlotModal();
            fetchData();
        } catch (error) {
            console.error("Error changing slot:", error);
        }
    };

    const handleDownloadReport = async (procedureId) => {
        try {
            const response = await axiosInstance.get(`/procedures/report/${procedureId}`);
            window.open(response.data, "_blank");
        } catch (error) {
            console.error("Error downloading report:", error);
        }
    };

    const getProcedureColor = (type) => {
        switch (type) {
            case "DIAGNOSIS":
                return "#FF6B6B";
            case "TREATMENT":
                return "#4ECDC4";
            case "EXAMINATION":
                return "#45B7D1";
            case "PROCEDURE":
                return "#96CEB4";
            case "SURGERY":
                return "#FFEEAD";
            default:
                return "#D3D3D3";
        }
    };

    if (loading) {
        return <div className="loading-overlay">Loading...</div>;
    }

    if (error) {
        return <div className="error-overlay">{error}</div>;
    }

    if (!petInfo) {
        return <div>No pet information found.</div>;
    }

    return (
        <div>
            <Header />
            <div className="container mt-2" style={{ display: "flex", gap: "100px", paddingTop: "100px" }}>
                <div className="ps-3">
                    <PetInfo petInfo={petInfo} onEdit={() => setIsEditModalOpen(true)} />

                    <div className="bg-treatment container mt-3 rounded-1 upcoming-appointments" style={{ padding: "20px" }}>
                        <h4>Upcoming Appointments</h4>
                        {upcomingAppointments.length > 0 ? (
                            <table cellPadding="3" cellSpacing="0">
                                <tbody>
                                {upcomingAppointments.map((appointment) => (
                                    <tr key={appointment.id}>
                                        <td>{new Date(appointment.slot.date).toLocaleDateString()}</td>
                                        <td>{appointment.slot.startTime.slice(0, 5)}</td>
                                        <td>-</td>
                                        <td>Dr. {appointment.slot.vetName}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        ) : (
                            <p>No upcoming appointments found.</p>
                        )}
                    </div>
                </div>

                <div style={{ flex: 1 }}>
                    <h2>Anamneses</h2>
                    <div className="bg-table element-space">
                        {anamneses.length > 0 ? (
                            <table cellPadding="5" cellSpacing="0" className="uniq-table">
                                <tbody>
                                {anamneses.map((anamnesis) => (
                                    <tr key={anamnesis.id}>
                                        <td>{new Date(anamnesis.date).toLocaleDateString()}</td>
                                        <td>{anamnesis.description}</td>
                                        <td>
                                            <button
                                                className="button btn-no-border"
                                                onClick={() => navigate(`/anamnesis/${anamnesis.id}`)}
                                            >
                                                More info
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        ) : (
                            <p>No anamneses found.</p>
                        )}
                        {userRole === "ROLE_VET" && (
                            <button
                                className="button rounded-3 btn-no-border"
                                onClick={() => setIsAddAnamnesisModalOpen(true)}
                            >
                                Add New Anamnesis
                            </button>
                        )}
                    </div>

                    <h2 style={{ marginTop: "30px" }}>Health Updates</h2>
                    <div className="bg-table">
                        {healthUpdates.length > 0 ? (
                            <table cellPadding="5" cellSpacing="0" className="uniq-table">
                                <tbody>
                                {healthUpdates.map((update) => (
                                    <tr key={update.id}>
                                        <td>{new Date(update.date).toLocaleDateString()}</td>
                                        <td>{update.dynamics ? "positive" : "negative"} dynamic</td>
                                        <td>
                                            <button
                                                className="button btn-no-border"
                                                onClick={() => handleViewHealthUpdateDetails(update.id)}
                                            >
                                                More info
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        ) : (
                            <p>No health updates found.</p>
                        )}
                        <button
                            className="button rounded-3 btn-no-border"
                            onClick={() => setIsAddHealthUpdateModalOpen(true)}
                        >
                            Add Health Update
                        </button>
                    </div>
                    <h2 style={{ marginTop: "30px" }}>Procedure Timeline</h2>
                    <div className="bg-table element-space" style={{ padding: "20px" }}>
                        {procedures.length > 0 ? (
                            <>
                                <div style={{ position: "relative", height: "100px", overflowX: "auto", padding: "0 40px" }}>
                                    <div
                                        style={{
                                            position: "absolute",
                                            top: "50%",
                                            left: "20px",
                                            right: "20px",
                                            height: "2px",
                                            backgroundColor: "#000",
                                        }}
                                    />
                                    {procedures
                                        .sort((a, b) => new Date(a.date) - new Date(b.date))
                                        .map((procedure, index) => {
                                            const date = new Date(procedure.date);
                                            const totalProcedures = procedures.length;
                                            const marginPercentage = 10;
                                            const adjustedPosition =
                                                totalProcedures > 1
                                                    ? `${marginPercentage + (index / (totalProcedures - 1)) * (100 - 2 * marginPercentage)}%`
                                                    : "50%";
                                            return (
                                                <div
                                                    key={procedure.id}
                                                    style={{
                                                        position: "absolute",
                                                        left: adjustedPosition,
                                                        top: "50%",
                                                        transform: "translate(-50%, -50%)",
                                                        width: "20px",
                                                        height: "20px",
                                                        backgroundColor: getProcedureColor(procedure.type),
                                                        borderRadius: "50%",
                                                        cursor: "pointer",
                                                    }}
                                                    title={`${procedure.name} (${date.toLocaleDateString()})`}
                                                    onClick={() => handleDownloadReport(procedure.id)}
                                                >
                                                    <span
                                                        style={{
                                                            position: "absolute",
                                                            top: "-25px",
                                                            left: "50%",
                                                            transform: "translateX(-50%)",
                                                            fontSize: "12px",
                                                            whiteSpace: "nowrap",
                                                        }}
                                                    >
                                                        {date.toLocaleDateString()}
                                                    </span>
                                                </div>
                                            );
                                        })}
                                </div>
                                <div style={{ marginTop: "20px" }}>
                                    <h4>Legend</h4>
                                    <div style={{ display: "flex", flexWrap: "wrap", gap: "15px" }}>
                                        <div style={{ display: "flex", alignItems: "center" }}>
                                            <div
                                                style={{
                                                    width: "15px",
                                                    height: "15px",
                                                    backgroundColor: "#FF6B6B",
                                                    borderRadius: "50%",
                                                    marginRight: "5px",
                                                }}
                                            />
                                            <span>Diagnosis</span>
                                        </div>
                                        <div style={{ display: "flex", alignItems: "center" }}>
                                            <div
                                                style={{
                                                    width: "15px",
                                                    height: "15px",
                                                    backgroundColor: "#4ECDC4",
                                                    borderRadius: "50%",
                                                    marginRight: "5px",
                                                }}
                                            />
                                            <span>Treatment</span>
                                        </div>
                                        <div style={{ display: "flex", alignItems: "center" }}>
                                            <div
                                                style={{
                                                    width: "15px",
                                                    height: "15px",
                                                    backgroundColor: "#45B7D1",
                                                    borderRadius: "50%",
                                                    marginRight: "5px",
                                                }}
                                            />
                                            <span>Examination</span>
                                        </div>
                                        <div style={{ display: "flex", alignItems: "center" }}>
                                            <div
                                                style={{
                                                    width: "15px",
                                                    height: "15px",
                                                    backgroundColor: "#96CEB4",
                                                    borderRadius: "50%",
                                                    marginRight: "5px",
                                                }}
                                            />
                                            <span>Procedure</span>
                                        </div>
                                        <div style={{ display: "flex", alignItems: "center" }}>
                                            <div
                                                style={{
                                                    width: "15px",
                                                    height: "15px",
                                                    backgroundColor: "#9c1212",
                                                    borderRadius: "50%",
                                                    marginRight: "5px",
                                                }}
                                            />
                                            <span>Surgery</span>
                                        </div>
                                        <div style={{ display: "flex", alignItems: "center" }}>
                                            <div
                                                style={{
                                                    width: "15px",
                                                    height: "15px",
                                                    backgroundColor: "#D3D3D3",
                                                    borderRadius: "50%",
                                                    marginRight: "5px",
                                                }}
                                            />
                                            <span>Other</span>
                                        </div>
                                    </div>
                                </div>
                            </>
                        ) : (
                            <p>No procedures found.</p>
                        )}
                    </div>
                </div>

                <div className="bg-treatment mt-1 rounded-1" style={{ padding: "20px" }}>
                    <h4>Treatment Recommendations</h4>
                    {treatments.length > 0 ? (
                        <table cellPadding="3" cellSpacing="0" className="uniq-table">
                            <tbody>
                            {treatments.map((treatment) => (
                                <tr key={treatment.id}>
                                    <td>
                                        <b>Treatment</b>: {treatment.name || "not specified"} <br />
                                        <b>Description</b>: {treatment.description || "not specified"} <br />
                                        <b>Prescribed Medication</b>: {treatment.prescribedMedication || "not specified"} <br />
                                        <b>Duration</b>: {treatment.duration || "not specified" } <br />
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    ) : (
                        <p>No treatment recommendations found.</p>
                    )}
                </div>

                {isEditModalOpen && (
                    <EditPetModal
                        petInfo={petInfo}
                        onClose={() => setIsEditModalOpen(false)}
                        onSave={handleSavePet}
                    />
                )}

                {isAddAnamnesisModalOpen && (
                    <AddAnamnesisModal
                        petId={petId}
                        onClose={() => setIsAddAnamnesisModalOpen(false)}
                        onSave={handleSaveAnamnesis}
                    />
                )}

                {isAddHealthUpdateModalOpen && (
                    <AddHealthUpdateModal
                        petId={petId}
                        onClose={() => setIsAddHealthUpdateModalOpen(false)}
                        onSave={handleSaveHealthUpdate}
                    />
                )}

                {isHealthUpdateDetailsModalOpen && (
                    <HealthUpdateDetailsModal
                        id={selectedHealthUpdateId}
                        onClose={() => setIsHealthUpdateDetailsModalOpen(false)}
                    />
                )}

                {isChangeSlotModalOpen && (
                    <div style={modalOverlayStyles}>
                        <div style={modalStyles} onClick={(e) => e.stopPropagation()}>
                            <h3>Change Appointment Slot</h3>
                            {availableSlots.length > 0 ? (
                                <div style={{ maxHeight: "300px", overflowY: "auto" }}>
                                    {availableSlots.map((slot) => (
                                        <div key={slot.id} style={{ marginBottom: "10px" }}>
                                            <label>
                                                <input
                                                    type="radio"
                                                    name="newSlot"
                                                    value={slot.id}
                                                    checked={newSlotId === slot.id}
                                                    onChange={() => setNewSlotId(slot.id)}
                                                />
                                                {new Date(slot.date).toLocaleDateString()} {slot.startTime} - {slot.endTime}
                                            </label>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p>No available priority slots found.</p>
                            )}
                            <div style={{ display: "flex", gap: "10px", marginTop: "20px" }}>
                                <button onClick={handleChangeSlot} disabled={!newSlotId}>
                                    Confirm Change
                                </button>
                                <button onClick={closeChangeSlotModal}>Cancel</button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

const modalOverlayStyles = {
    position: "fixed",
    top: 0,
    left: 0,
    width: "100%",
    height: "100%",
    backgroundColor: "rgba(0, 0, 0, 0.5)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    zIndex: 1000,
};

const modalStyles = {
    backgroundColor: "white",
    padding: "20px",
    borderRadius: "10px",
    boxShadow: "0 4px 8px rgba(0, 0, 0, 0.2)",
    maxWidth: "500px",
    width: "90%",
    maxHeight: "80vh",
    overflowY: "auto",
};

export default PetProfilePage;