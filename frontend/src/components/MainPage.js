import { useNavigate } from "react-router-dom";
import { useAuth } from "../AuthProvider";
import Header from "./Header";
import '../sidebar.css';
import myImage from "../pics/logo_min.png";
import React, {useState, useEffect} from "react";
import AppointmentPage from "./AppointmentPage";
import useAxiosWithAuth from "../AxiosAuth";
import AddReviewModal from "./AddReviewModal";
import SeeAllReviewsModal from "./SeeAllReviewsModal";
import { Swiper, SwiperSlide } from "swiper/react";
import "swiper/css";
import "swiper/css/navigation";
import { Navigation } from "swiper/modules";
import axios from "axios";
import KegaSideBar from "../pics/kega.png";
import PawStub from "../pics/paw.png";

export default function HomePage() {
    const navigate = useNavigate();
    const { token: authToken } = useAuth();
    const storedToken = localStorage.getItem("token");
    const token = authToken || storedToken;
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [vets, setVets] = useState([]);
    const [vetRatings, setVetRatings] = useState({});
    const [loading, setLoading] = useState(true);
    const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
    const [isSeeAllReviewsModalOpen, setIsSeeAllReviewsModalOpen] = useState(false);
    const [selectedVetId, setSelectedVetId] = useState(null);
    const [selectedVetName, setSelectedVetName] = useState("");
    const [selectedVet, setSelectedVet] = useState(null);
    const [ownerId, setOwnerId] = useState(null);
    const axiosInstance = useAxiosWithAuth();

    useEffect(() => {
        const fetchVetsAndRatings = async () => {
            setLoading(true);
            try {
                const vetsResponse = await axiosInstance.get("/users/all-vets");
                const vetsData = vetsResponse.data;
                setVets(vetsData);

                const ratingsPromises = vetsData.map((vet) =>
                    axiosInstance.get(`/rating-and-reviews/by-vet/${vet.id}`).then((response) => ({
                        vetId: vet.id,
                        ratings: response.data,
                    }))
                );
                const ratingsResults = await Promise.all(ratingsPromises);

                const ratingsMap = ratingsResults.reduce((acc, result) => {
                    acc[result.vetId] = result.ratings;
                    return acc;
                }, {});
                setVetRatings(ratingsMap);

                if (token) {
                    const userResponse = await axiosInstance.get("/users/current-user-info");
                    setOwnerId(userResponse.data.id);
                }
            } catch (error) {
                console.error("Error fetching vets or ratings:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchVetsAndRatings();
    }, [axiosInstance, token]);

    const handleAppointmentClick = () => {
        if (token) {
            setIsModalOpen(true);
        } else {
            navigate("/login", { state: { redirectTo: "/appointment" } });
        }
    };

    const closeModal = () => {
        setIsModalOpen(false);
    };

    const getAverageRating = (ratings) => {
        if (!ratings || ratings.length === 0) return 0;
        const total = ratings.reduce((sum, rating) => sum + rating.rating, 0);
        return (total / ratings.length).toFixed(1);
    };

    const renderStars = (rating) => {
        const stars = Math.round(rating);
        return "⭐".repeat(stars) + "✯".repeat(5 - stars); //return "⭐".repeat(stars) + "✩".repeat(5 - stars);
    };

    const openReviewModal = (vetId, vetName) => {
        if (!token) {
            navigate("/login", { state: { redirectTo: "/" } });
            return;
        }
        setSelectedVetId(vetId);
        setSelectedVetName(vetName);
        setIsReviewModalOpen(true);
    };

    const closeReviewModal = () => {
        setIsReviewModalOpen(false);
        setSelectedVetId(null);
        setSelectedVetName("");
    };

    const handleSaveReview = (newReview) => {
        setVetRatings((prev) => ({
            ...prev,
            [newReview.vet]: [...(prev[newReview.vet] || []), newReview],
        }));
    };

    const openSeeAllReviewsModal = (vet) => {
        setSelectedVet(vet);
        setIsSeeAllReviewsModalOpen(true);
    };

    const closeSeeAllReviewsModal = () => {
        setIsSeeAllReviewsModalOpen(false);
        setSelectedVet(null);
    };

    return (
        <div>
            <Header/>
            <div id="dog-sidebar">
                <img src={KegaSideBar} alt="Cute Dog"/>
            </div>
            <div style={{flex: 1}}>
                <div className="main-container mt-2" style={{display: "flex", gap: "5px", paddingTop: '80px'}}>
                    <div className="bg-table centered-content"
                         style={{position: "relative", flex: 1, margin: "20px", padding: "20px"}}>
                        <div
                            style={{
                                position: "absolute",
                                left: 20,
                                top: 20,
                                bottom: 0,
                                width: "210px",
                                height: "210px",
                                backgroundImage: `url(${myImage})`,
                                backgroundSize: "cover",
                                backgroundPosition: "center",
                                backgroundRepeat: "no-repeat",
                            }}
                        />
                        <h1 style={{marginBottom: "10px", textAlign: "left", paddingRight: "950px"}}>Welcome to
                            VetCare!</h1>
                        <p style={{
                            marginBottom: "15px",
                            fontSize: "25px",
                            textAlign: "left",
                            paddingLeft: "250px",
                            paddingRight: "80px"
                        }}>
                            VetCare is a modern service for booking veterinary appointments, managing pet health,
                            and receiving online medical care. We offer convenient appointment scheduling, access to
                            your pet's medical history, and clinic
                            news.
                        </p>

                        <button
                            style={{padding: "12px 30px", fontSize: "17px"}}
                            className="button btn-no-border rounded-4"
                            onClick={handleAppointmentClick}
                        >
                            <b>Book an Appointment</b>
                        </button>
                    </div>
                    <h2>Meet Our Veterinarians</h2>
                    <div className="bg-vet" style={{flex: 1, margin: '0px', padding: '20px'}}>
                        {loading ? (
                            <p>Loading veterinarians...</p>
                        ) : vets.length > 0 ? (
                            <div style={{padding: '0 20px'}}>
                                <Swiper
                                    slidesPerView={4}
                                    spaceBetween={10}
                                    navigation={true}
                                    modules={[Navigation]}
                                    breakpoints={{
                                        320: {slidesPerView: 1},
                                        768: {slidesPerView: 2},
                                        1024: {slidesPerView: 4},
                                    }}
                                >
                                    {vets.map((vet) => {
                                        const ratings = vetRatings[vet.id] || [];
                                        const averageRating = getAverageRating(ratings);
                                        const review = ratings.length > 0 ? ratings[ratings.length - 1].review : "No reviews yet";

                                        return (
                                            <SwiperSlide key={vet.id}>
                                                <div style={{
                                                    marginBottom: "20px",
                                                    padding: '10px',
                                                    textAlign: 'center'
                                                }}>
                                                    {vet.photoUrl ? (
                                                        <img
                                                            className="avatar"
                                                            src={vet.photoUrl}
                                                            alt={`${vet.name}'s avatar`}
                                                            style={{
                                                                width: "200px",
                                                                height: "200px",
                                                                borderRadius: "50%"
                                                            }}
                                                        />
                                                    ) : (
                                                        <img
                                                            className="avatar"
                                                            src={PawStub}
                                                            alt={`photo stub`}
                                                            style={{
                                                                width: "200px",
                                                                height: "200px",
                                                                borderRadius: "50%"
                                                            }}
                                                        />
                                                    )}
                                                    <h3 style={{marginTop: "15px"}}>Dr. {vet.name} {vet.surname}</h3>
                                                    <h6>{vet.qualification || "Expert in Veterinary Medicine"}</h6>
                                                    <div
                                                        style={{margin: '5px 0'}}>
                                                        <p style={{marginBottom: "5px"}}>
                                                            {renderStars(averageRating)} ({averageRating})
                                                        </p>
                                                    </div>
                                                    <p style={{marginBottom: "5px"}}>
                                                        <strong>Latest Review:</strong> "{review}"
                                                    </p>
                                                    <div style={{
                                                        display: "flex",
                                                        gap: "10px",
                                                        justifyContent: 'center'
                                                    }}>
                                                        <button
                                                            className="button btn-no-border rounded-3"
                                                            onClick={() => openReviewModal(vet.id, `${vet.name} ${vet.surname}`)}
                                                        >
                                                            Leave a Review
                                                        </button>
                                                        <button
                                                            className="button btn-no-border rounded-3"
                                                            onClick={() => openSeeAllReviewsModal(vet)}
                                                        >
                                                            See all Reviews
                                                        </button>
                                                    </div>
                                                </div>
                                            </SwiperSlide>
                                        );
                                    })}
                                </Swiper>
                            </div>
                        ) : (
                            <p>No veterinarians found.</p>
                        )}
                    </div>
                </div>
                <footer className="footer" style={{
                    backgroundColor: 'rgba(131,61,59,0.69)',
                    padding: '30px',
                    marginTop: '20px',
                    textAlign: 'center',
                    borderTop: '1px solid #e9ecef',
                    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.5)',
                }}>
                    <div className="container">
                        <h2 style={{marginBottom: '20px', fontSize: '24px', color: '#fff3f3'}}>Contact Information</h2>
                        <p style={{margin: '5px 0', fontSize: '16px', color: '#ffe9e9'}}>Address: 10 Veterinary Street,
                            Moscow</p>
                        <p style={{margin: '5px 0', fontSize: '16px', color: '#fff3f3'}}>Phone: +8 (800) 555-35-35</p>
                        <p style={{margin: '5px 0', fontSize: '16px', color: '#fff3f3'}}>Email: contact@vetcare.ru</p>
                    </div>
                </footer>
            </div>

            {isModalOpen && (
                <div
                    style={{
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
                    }}
                >
                    <div
                        style={{
                            backgroundColor: "white",
                            padding: "20px",
                            borderRadius: "8px",
                            width: "80%",
                            maxWidth: "800px",
                            maxHeight: "80vh",
                            overflowY: "auto",
                            position: "relative",
                        }}
                    >
                        <button
                            onClick={closeModal}
                            style={{
                                position: "absolute",
                                top: "10px",
                                right: "10px",
                                border: "none",
                                background: "transparent",
                                fontSize: "20px",
                                cursor: "pointer",
                            }}
                        >
                            ✕
                        </button>
                        <AppointmentPage onClose={closeModal}/>
                    </div>
                </div>
            )}

            {isReviewModalOpen && (
                <AddReviewModal
                    vetId={selectedVetId}
                    vetName={selectedVetName}
                    ownerId={ownerId}
                    onClose={closeReviewModal}
                    onSave={handleSaveReview}
                />
            )}

            {isSeeAllReviewsModalOpen && (
                <SeeAllReviewsModal
                    vet={selectedVet}
                    ratings={vetRatings[selectedVet?.id] || []}
                    onClose={closeSeeAllReviewsModal}
                />
            )}
        </div>
    );
}