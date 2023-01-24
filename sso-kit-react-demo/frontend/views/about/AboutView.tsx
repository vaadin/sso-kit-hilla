import { AuthContext } from "Frontend/useAuth.js";
import { useContext } from "react";

export default function AboutView() {
  const { state } = useContext(AuthContext);
  return (
    <div className="flex flex-col h-full items-center justify-center p-l text-center box-border">
      <img style={{ width: '200px' }} src="images/empty-plant.png" />
      <h2>This place is about you</h2>
      <p>Username: {state.user!.preferredUsername}</p>
      <p>Full name: {state.user!.fullName}</p>
      <p>Email: {state.user!.email}</p>
      <p>Roles: {state.user!.roles?.join(', ')}</p>
    </div>
  );
}
